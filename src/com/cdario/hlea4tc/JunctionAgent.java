package com.cdario.hlea4tc;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;
import java.util.Vector;

/*
 * Implements the initiator roles in FIPA-Subscribe IP.
 */
public class JunctionAgent extends Agent {

    protected String junctionID;
    protected AID mySector;
    protected ArrayList<AID> knownSectors;

    @Override
    protected void setup() {

        knownSectors = new ArrayList<AID>();
        junctionID = getLocalName();

        /*
         *      Behaviour: update known sectors by subscribing to DF
         */

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sdd = new ServiceDescription();
        sdd.setName("sector-registration");
        sdd.setType("DF-Subscriptions");
        template.addServices(sdd);
        Behaviour sectorUpdater = new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            @Override
            protected void handleInform(ACLMessage inform) {
                try {
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());

                    for (int i = 0; i < dfds.length; i++) {
                        if (!dfds[i].getName().equals(myAgent.getAID())) // know thyself TODO: unneeded here, but when refactoring; only sectors register
                        {
                            knownSectors.add(dfds[i].getName());
                        }
                    }
                    System.out.println("[J] " + getLocalName() + "\t # acknowledges " + knownSectors.size() + " sector(s)");
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        };
        addBehaviour(sectorUpdater);

        /*
         * attempt subscribe every 5 seconds
         */

        addBehaviour(new TickerBehaviour(this, 8000) {
            @Override
            protected void onTick() {
                if (knownSectors != null && knownSectors.size() > 0) {
                    addBehaviour(new JunctionSubscriptionInit(myAgent));
                } else {
                    System.out.println("No responder specified.");
                }
            }
        });


    }

    class JunctionSubscriptionInit extends SubscriptionInitiator {
        
        JunctionSubscriptionInit(Agent agent) { 
            super(agent, new ACLMessage(ACLMessage.SUBSCRIBE));
        }
        
        /*  Vector is deprecated, but JADE API requires it    */
        @Override 
        protected Vector<ACLMessage> prepareSubscriptions(ACLMessage subscription) {
            subscription.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
            //subscription.setConversationId("sector-subscription");
            for (int i = 0; i < knownSectors.size(); ++i) {
                subscription.addReceiver(knownSectors.get(i));   // the agent supplying a subscription service (has a responder role)
            }
            System.out.println("[J] " + myAgent.getLocalName() + "\t >> SUBSCRIPTION request to sectors (x" + knownSectors.size() + ")");

            subscription.setContent("subscription-request");   // the subscription content
            subscription.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            Vector<ACLMessage> v = new Vector<ACLMessage>();
            v.addElement(subscription);
            return v;
        }

        
        /*
         *  Interested in accept-propsal, reject proposal performatives 
         */
//        @Override
//        protected void handleAllResponses(Vector responses) {
//            
//            for (int i=0; i<responses.size(); i++)
//            {
//                ACLMessage thisMsg = (ACLMessage)responses.get(i);
//                if (thisMsg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
//                {
//                  System.out.println("[J] " + myAgent.getLocalName() + "\t ! SUBSCRIBED to " + thisMsg.getSender().getLocalName());  
//                }
//                
//                
//                // THIS IF...
//                if (thisMsg.getPerformative() == ACLMessage.INFORM)
//                    
//                {
//                  System.out.println("[J] " + myAgent.getLocalName() + "\t & INFORMED " + thisMsg.getContent());  
//                }
//            }
//        }
        

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Junction " + myAgent.getLocalName() + ": Sector " + refuse.getSender().getLocalName() + " refused " + getLocalName() + "'s subscription");
        }

        @Override
        protected void handleAgree(ACLMessage agree) {

            //TODO: unregister from other SECTORS here
            // see cancel-meta protocol

            if (mySector == null) {
                mySector = agree.getSender();   //set new sector
            } else {   // sector already set

                if (!mySector.getName().equals(agree.getSender().getName())) {

                    cancel(mySector, true);  // cancel old subscription and do ignore confirmation
                    cancellationCompleted(mySector);    //TODO: upgrade to SubscriptionManager in handleCancel receiver
                    AID oldSector = mySector;
                    mySector = agree.getSender();   //update to new sector
                    System.out.println("Junction " + myAgent.getLocalName() + ": Reallocated from Sector " + oldSector.getLocalName() + " to " + mySector.getLocalName());
//                    System.out.println("Junction " + myAgent.getLocalName() + ": Sector " + agree.getSender().getLocalName() + " agreed to " + getLocalName() + "'s subscription");
                } else {   //same sector
                    System.out.println("Junction " + getLocalName() + ": Stays in Sector " + agree.getSender().getLocalName());
                }
            }
        }

        @Override
        protected void handleInform(ACLMessage inform) {

            if (inform.getSender().getLocalName().equals(mySector.getLocalName())) // TODOD: disregard messages from other SECTORS
            {
                System.out.println(">>> Junction " + myAgent.getLocalName() + "[in " + mySector.getLocalName() + "]: Sector says --> " + inform.getContent());
            }
        }

        @Override
        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                // FAILURE notification from the JADE runtime: the receiver does not exist
                System.out.println("Responder does not exist");
            } else {
                System.out.println("Junction " + myAgent.getLocalName() + ": Sector " + failure.getSender().getLocalName() + " failed to perform the requested action");
            }
        }

        protected void handleAllResultNotifications(ArrayList notifications) {
            if (notifications.size() < knownSectors.size()) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing " + (knownSectors.size() - notifications.size()) + " responses");
            }
        }
    }

    String timestamp() {
        StringBuilder sb = new StringBuilder();
        Formatter format = new Formatter(sb, Locale.ENGLISH);
        format.format("%tT ", Calendar.getInstance());
        return sb.toString();
    }
}
