package com.cdario.hlea4tc;

import com.cdario.hlea4tc.contractnet1.Intersection;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
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

/**
 *
 * @author cdario
 */

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
         *      Behaviour: update known by subscribing to DF
         */
        
        //addBehaviour(new SectorManager(this, 5000, new Date())); // every 5s
                
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sdd = new ServiceDescription();
        sdd.setType("sector-registration");
        template.addServices(sdd);       
        Behaviour sectorUpdater = new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) 
        {
         @Override
	 protected void handleInform(ACLMessage inform) {
		try {
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    //knownSectors = new AID[dfds.length];  // not self
                    for (int i = 0; i < dfds.length; i++) {
                       if (!dfds[i].getName().equals(myAgent.getAID()))   // know thyself
                       {
                           //System.out.println("IF != " + dfds[i].getName()+" - "+ myAgent.getAID());
                           //knownSectors[i] = dfds[i].getName();
                           knownSectors.add(dfds[i].getName());
                       }
                   }
                    System.out.println("Junction " + getLocalName() + ": "+knownSectors.size()+" sector(s) known");
		}
		catch (FIPAException fe) {
		  fe.printStackTrace();
		}
	  }
        };
        addBehaviour(sectorUpdater);
        
        
        
        /*
         * attempt subscribe every 5 seconds
         */
        
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                if (knownSectors != null && knownSectors.size() > 0) {
                    addBehaviour(new JunctionSubscriptionInit(myAgent));
                } else {
                    System.out.println("No responder specified.");
                }
            }
        });
              
           /*
         * attempt subscribe one 10 seconds after creation
         */
//        addBehaviour(new WakerBehaviour(this, 10000) {
//            @Override
//            protected void onWake() {
//                if (knownSectors != null && knownSectors.size() > 0) {
//                    addBehaviour(new JunctionSubscriptionInit(myAgent));
//                } else {
//                    System.out.println("No responder specified.");
//                }
//            }
//        });

//        addBehaviour(new TickerBehaviour(this, 10000) {
//            @Override
//            protected void onTick() {
//                
//            }
//        });
    }

    class JunctionSubscriptionInit extends SubscriptionInitiator {

        JunctionSubscriptionInit(Agent agent) {
            super(agent, new ACLMessage(ACLMessage.SUBSCRIBE));
        }

        /*  Vector is deprecated, but JADE API requires it    */
        @Override
        protected Vector<ACLMessage> prepareSubscriptions(ACLMessage subscription) {
            subscription.setProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE);
            for (int i = 0; i < knownSectors.size(); ++i) {
                subscription.addReceiver(knownSectors.get(i));   // the agent supplying a subscription service (has a responder role)
            }
            System.out.println("Junction "+myAgent.getLocalName()+": Requesting subscription to " + knownSectors.size() + " responders.");

            subscription.setContent("subscription-request");   // the subscription content
            subscription.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            Vector<ACLMessage> v = new Vector<ACLMessage>();
            v.addElement(subscription);
            return v;
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Junction "+myAgent.getLocalName()+": Sector " + refuse.getSender().getLocalName() + " refused " + getLocalName() + "'s subscription");
        }

        @Override
        protected void handleAgree(ACLMessage agree) {
            mySector = agree.getSender();
            System.out.println("Junction "+myAgent.getLocalName()+": Sector " + agree.getSender().getLocalName() + " agreed to " + getLocalName() + "'s subscription");
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println("Junction "+myAgent.getLocalName()+": Sector " + inform.getSender().getLocalName() + " informs that --> " + inform.getContent());
        }

        @Override
        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                // FAILURE notification from the JADE runtime: the receiver does not exist
                System.out.println("Responder does not exist");
            } else {
                System.out.println("Junction "+myAgent.getLocalName()+": Sector " + failure.getSender().getLocalName() + " failed to perform the requested action");
            }
        }

        protected void handleAllResultNotifications(ArrayList notifications) {
            if (notifications.size() < knownSectors.size()) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing " + (knownSectors.size() - notifications.size()) + " responses");
            }
        }
    }

//    private class SectorManager extends TickerBehaviour {
//
//        private long deadline, initTime, deltaT;
//
//        public SectorManager(Agent a, long period, Date d) {
//            super(a, period);
//            deadline = d.getTime();
//            initTime = System.currentTimeMillis();
//            deltaT = deadline - initTime;
//        }
//
//        @Override
//        protected void onTick() {
//
//            /*  update list of available sectors */
//            DFAgentDescription template = new DFAgentDescription();
//            ServiceDescription sd = new ServiceDescription();
//            sd.setType("sector-registration");
//            template.addServices(sd);
//            try {
//                DFAgentDescription[] res = DFService.search(myAgent, template);
//                //knownSectors = new AID[res.length];
//                knownSectors = new ArrayList<AID>(res.length);
//                for (int i = 0; i < res.length; i++) {
//                    knownSectors.set(i, res[i].getName());
//                    //knownSectors[i] = res[i].getName();
//                }
//            } catch (FIPAException e) {
//                e.printStackTrace();
//            }
//            //System.out.println(timestamp() + " [" + myAgent.getAID().getLocalName() + "] knows " + knownSectors.length + " sector(s)");
//
//        }
//    }

    String timestamp() {
        StringBuilder sb = new StringBuilder();
        Formatter format = new Formatter(sb, Locale.ENGLISH);
        format.format("%tT ", Calendar.getInstance());
        return sb.toString();
    }
}
