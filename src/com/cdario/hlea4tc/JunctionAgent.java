package com.cdario.hlea4tc;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
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

    @Override
    protected void setup() {

        // Read names of responders as arguments
        //Object[] args = getArguments();
        //TODO: Get responder-sectors names from Directory        

        junctionID = getLocalName();
        addBehaviour(new SectorManager(this, 20000, new Date())); // every 20s
        if (knownSectors.length > 0) {
            addBehaviour(new JunctionSubscriptionInit(this));
        } else {
            System.out.println("No responder specified.");
        }

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
            System.out.println("Requesting subscription to " + knownSectors.length + " responders.");

            subscription.setContent("subscription-request");   // the subscription content
            subscription.setReplyByDate(new Date(System.currentTimeMillis() + 10000));
            Vector<ACLMessage> v = new Vector<ACLMessage>();
            v.addElement(subscription);
            return v;
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Agent " + refuse.getSender().getName() + " refused " + getLocalName() + "'s subscription");
        }

        @Override
        protected void handleAgree(ACLMessage agree) {
            System.out.println("Agent " + agree.getSender().getName() + " agreed to " + getLocalName() + "'s subscription");
        }

        @Override
        protected void handleInform(ACLMessage inform) {
            System.out.println("Agent " + inform.getSender().getName() + " successfully performed the requested action with " + inform.getContent());
        }

        @Override
        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                // FAILURE notification from the JADE runtime: the receiver
                // does not exist
                System.out.println("Responder does not exist");
            } else {
                System.out.println("Agent " + failure.getSender().getName() + " failed to perform the requested action");
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
            System.out.println(timestamp() + " [" + myAgent.getAID().getLocalName() + "] knows" + knownSectors.length + " sectors");

        }
    }

    String timestamp() {
        StringBuilder sb = new StringBuilder();
        Formatter format = new Formatter(sb, Locale.ENGLISH);
        format.format("%tT ", Calendar.getInstance());
        return sb.toString();
    }
}
