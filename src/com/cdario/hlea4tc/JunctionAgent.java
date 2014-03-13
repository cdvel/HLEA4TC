package com.cdario.hlea4tc;

import com.cdario.hlea4tc.contractnet1.Intersection;
import jade.core.AID;
import jade.core.Agent;
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
    protected AID[] knownSectors;
    private Agent self;

    @Override
    protected void setup() {

        // Read names of responders as arguments
        //Object[] args = getArguments();
        //TODO: Get responder-sectors names from Directory  

        knownSectors = null;
        junctionID = getLocalName();
        self = this;
        addBehaviour(new SectorManager(this, 5000, new Date())); // every 5s

        addBehaviour(new WakerBehaviour(self, 10000) {
            @Override
            protected void onWake() {
                if (knownSectors != null && knownSectors.length > 0) {
                    addBehaviour(new JunctionSubscriptionInit(self));
                } else {
                    System.out.println("No responder specified.");
                }
            }
        });

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
            for (int i = 0; i < knownSectors.length; ++i) {
                subscription.addReceiver(knownSectors[i]);   // the agent supplying a subscription service (has a responder role)
            }
            System.out.println("Junction "+myAgent.getLocalName()+": Requesting subscription to " + knownSectors.length + " responders.");

            subscription.setContent("subscription-request");   // the subscription content
            subscription.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            Vector<ACLMessage> v = new Vector<ACLMessage>();
            v.addElement(subscription);
            return v;
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Junction "+myAgent.getLocalName()+"Sector " + refuse.getSender().getLocalName() + " refused " + getLocalName() + "'s subscription");
        }

        @Override
        protected void handleAgree(ACLMessage agree) {
            mySector = agree.getSender();
            System.out.println("Junction "+myAgent.getLocalName()+"Sector " + agree.getSender().getLocalName() + " agreed to " + getLocalName() + "'s subscription");
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println("Junction "+myAgent.getLocalName()+"Sector " + inform.getSender().getLocalName() + " informs that: " + inform.getContent());
        }

        @Override
        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                // FAILURE notification from the JADE runtime: the receiver does not exist
                System.out.println("Responder does not exist");
            } else {
                System.out.println("Junction "+myAgent.getLocalName()+"Sector " + failure.getSender().getLocalName() + " failed to perform the requested action");
            }
        }

        protected void handleAllResultNotifications(ArrayList notifications) {
            if (notifications.size() < knownSectors.length) {
                // Some responder didn't reply within the specified timeout
                System.out.println("Timeout expired: missing " + (knownSectors.length - notifications.size()) + " responses");
            }
        }
    }

    private class SectorManager extends TickerBehaviour {

        private long deadline, initTime, deltaT;

        public SectorManager(Agent a, long period, Date d) {
            super(a, period);
            deadline = d.getTime();
            initTime = System.currentTimeMillis();
            deltaT = deadline - initTime;
        }

        @Override
        protected void onTick() {

            /*  update list of available sectors */
            DFAgentDescription template = new DFAgentDescription();
            ServiceDescription sd = new ServiceDescription();
            sd.setType("sector-registration");
            template.addServices(sd);
            try {
                DFAgentDescription[] res = DFService.search(myAgent, template);
                knownSectors = new AID[res.length];
                for (int i = 0; i < res.length; i++) {
                    knownSectors[i] = res[i].getName();
                }
            } catch (FIPAException e) {
                e.printStackTrace();
            }
            //System.out.println(timestamp() + " [" + myAgent.getAID().getLocalName() + "] knows " + knownSectors.length + " sector(s)");

        }
    }

    String timestamp() {
        StringBuilder sb = new StringBuilder();
        Formatter format = new Formatter(sb, Locale.ENGLISH);
        format.format("%tT ", Calendar.getInstance());
        return sb.toString();
    }
}
