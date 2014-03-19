package com.cdario.hlea4tc;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import jade.proto.ProposeInitiator;
import jade.proto.ProposeResponder;
import jade.proto.SubscriptionInitiator;
import jade.proto.SubscriptionResponder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Locale;
import java.util.Vector;

/*
 * Implements the responder role in FIPA-Subscribe IP. 
 */
public class SectorAgent extends Agent {

    protected int sectorID;
    protected AID[] myJunctions;
    //protected AID[] knownSectors;
    protected ArrayList<AID> knownSectors;
    protected SectorSubscriptionResp subscriptionResponder;
    protected SectorProposalInit proposalInitiator;
    protected SectorProposalResp proposalResponder;

    @Override
    protected void setup() {

        knownSectors = new ArrayList<AID>();

        /*
         *      Behaviour: update known by subscribing to DF
         */

        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription sdd = new ServiceDescription();
        sdd.setType("sector-registration");
        template.addServices(sdd);
        Behaviour sectorUpdater = new SubscriptionInitiator(this, DFService.createSubscriptionMessage(this, getDefaultDF(), template, null)) {
            @Override
            protected void handleInform(ACLMessage inform) {
                try {
                    DFAgentDescription[] dfds = DFService.decodeNotification(inform.getContent());
                    for (int i = 0; i < dfds.length; i++) {
                        if (!dfds[i].getName().equals(myAgent.getAID())) // know thyself
                        {
                            knownSectors.add(dfds[i].getName());
                        }
                    }
                    System.out.println(getLocalName() + ": " + knownSectors.size() + " sector(s) known");
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
            }
        };
        addBehaviour(sectorUpdater);

        /*
         *      DF registration
         */

        DFAgentDescription DFAgDescription = new DFAgentDescription();
        DFAgDescription.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("sector-registration");
        sd.setName("HLE4TC");
        DFAgDescription.addServices(sd);
        try {
            DFService.register(this, DFAgDescription);
        } catch (Exception fe) {
            fe.printStackTrace();
        }

        /*
         *      Behaviour: respond to subscriptions and inform every 5 seconds
         */

        System.out.println("* "+getLocalName() + "\tis up and waiting for subscriptions...");
        //SubscriptionResponder.SubscriptionManager manager = new SubscriptionResponder(this, null).
        subscriptionResponder = new SectorSubscriptionResp(this);
        SectorProposalInit propInitiator = new SectorProposalInit(this, null);
        subscriptionResponder.registerHandleSubscription(propInitiator);
        addBehaviour(subscriptionResponder);    // pass handle to proposal behaviour
        
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.setContent(timestamp() + " adjust x secs (" + subscriptionResponder.getSubscriptions().size() + ")");
                //TODO: content is junction-specific?
                subscriptionResponder.notifyJunctions(inform);
            }
        });
        
        proposalResponder = new SectorProposalResp(this, null);
        addBehaviour(proposalResponder);

        /*
         *      Behaviour: negotiate junctions once? every 10 seconds?
         */

//        addBehaviour(new WakerBehaviour(this, 10000) {
//            @Override
//            protected void onWake() {
//
//                MessageTemplate contractTemplate = MessageTemplate.and(
//                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
//                    MessageTemplate.MatchPerformative(ACLMessage.CFP));
//
//                ACLMessage msgCFP = new ACLMessage(ACLMessage.CFP);
//                for (int i = 0; i < knownSectors.size(); ++i) {
//                    msgCFP.addReceiver(new AID((String) knownSectors.get(i).getLocalName(), AID.ISLOCALNAME));
//                }
//
//                msgCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
//                msgCFP.setReplyByDate(new Date(System.currentTimeMillis() + 10000)); // We want to receive a reply in 10 secs
//                msgCFP.setContent("sector-negotiation");    //TODO: intersection ID and contribution here?
//
////                negotiatorInit = new SectorJunctionNegotiatorInit(myAgent, msgCFP);
////                negotiatorResp = new SectorJunctionNegotiatorResp(myAgent, contractTemplate);
////
////                addBehaviour(negotiatorInit);
////                addBehaviour(negotiatorResp);
//
//            }
//        });
}

class SectorSubscriptionResp extends SubscriptionResponder {

        //TODO: check other constructor with SubscriptionManager
    SectorSubscriptionResp(Agent a) {
        super(a, MessageTemplate.and(
            MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
                MessageTemplate.MatchPerformative(ACLMessage.CANCEL)),
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)));

            // HERE PLUG IN propose initiator !!! pp 107 


    }



        
        



        // USE to nest?
        //        @Override
        //        public void registerHandleSubscription(Behaviour b) {
        //            super.registerHandleSubscription(b); //To change body of generated methods, choose Tools | Templates.
        //        }
    @Override
    protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {

        System.out.println(getLocalName() + " : Canceling subscription from " + cancel.getSender().getLocalName());
        Subscription subsc = getSubscription(cancel);
            //           if (subsc!=null)    //TODO: subscription 
            //             this.mySubscriptionManager.deregister(subsc);
            return null;             //return cancel;

        }

        @Override
        protected ACLMessage handleSubscription(ACLMessage subscriptionMsg) throws RefuseException {
            System.out.println(getLocalName() + ": Received subscription request from " + subscriptionMsg.getSender().getLocalName() + " [" + subscriptionMsg.getContent() + "]");
            ACLMessage reply = subscriptionMsg.createReply();

            // if successful, should answer (return) with AGREE; otherwise with REFUSE or NOT_UNDERSTOOD
            //TODO: implemend return for not-understood

            if (junctionFits(subscriptionMsg)) {
                /* negotiate (propose) with other sectors here */
                if (junctionAwarded(subscriptionMsg)) {
                    // We agree to perform the action. 
                    Vector previousSub = getSubscriptions(subscriptionMsg.getSender());   // 
                    if (previousSub.size() == 0) // one subscriptionMsg per junction
                    {
                        createSubscription(subscriptionMsg);
                    }
                    System.out.println(getLocalName() + ": AGREED subscription request from " + subscriptionMsg.getSender().getLocalName() + " [" + subscriptionMsg.getContent() + "]");
                    reply.setPerformative(ACLMessage.AGREE);

                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                }
            } else {
                // We refuse to perform the action
                System.out.println(getLocalName() + ": REFUSED subscription request from " + subscriptionMsg.getSender().getLocalName() + " [" + subscriptionMsg.getContent() + "]");
                reply.setPerformative(ACLMessage.REFUSE);
            }
            
            storeNotification(ACLMessage.SUBSCRIBE);
            return reply;
        }
        
        private void storeNotification(int performative) {
          if (performative == ACLMessage.SUBSCRIBE) {
             System.out.println("Agent "+getLocalName()+": subscription successful");
         }

					// Retrieve the incoming request from the DataStore
         String incomingSubscriptionkey = (String) ((SectorSubscriptionResp) parent).SUBSCRIPTION_KEY;
         ACLMessage incomingSubscription = (ACLMessage) getDataStore().get(incomingSubscriptionkey);
					// Prepare the notification to the request originator and store it in the DataStore
         ACLMessage notification = incomingSubscription.createReply();
         notification.setPerformative(performative);
         String notificationkey = (String) ((SectorSubscriptionResp) parent).RESPONSE_KEY;
         getDataStore().put(notificationkey, notification);
     }

     protected void notifyJunctions(ACLMessage inform) {
            // this is the method you invoke ("call-back") for creating a new inform message; not part of the SubscriptionResponder API, rename it

            // go through every subscription
            Vector subs = getSubscriptions(); // from stored by createSubscription
            for (int i = 0; i < subs.size(); i++) {
                ((SubscriptionResponder.Subscription) subs.elementAt(i)).notify(inform);
            }
        }

        private boolean junctionFits(ACLMessage subscription) {
            //TODO: evaluate subscription and decide wheter you want it
            /*
             * Decide initially by checking several factors such as:
             * 
             * 1. current no. of subscribe junctions
             * 2. how does the new junction fit with others, similar traff
             *    density? volume? contribution?
             *  
             */
            return (Math.random() > 0.5);
        }

        private boolean junctionAwarded(final ACLMessage subscription) {

            //TODO: run OneShot nested with ContractNetInit against all known Sectors
            /*
             * Here starts negotiation with other junctions
             * 
             */
//            addBehaviour(new OneShotBehaviour(myAgent) {
//                @Override
//                public void action() {
//                    /*
//                     * send CFPs to ll known sectors
//                     */
//                    ACLMessage msgCFP = new ACLMessage(ACLMessage.CFP);
//                    for (int i = 0; i < knownSectors.size(); ++i) {
//                        msgCFP.addReceiver(new AID((String) knownSectors.get(i).getLocalName(), AID.ISLOCALNAME));
//                    }
//
//                    msgCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
//                    msgCFP.setReplyByDate(new Date(System.currentTimeMillis() + 5000)); // We want to receive a reply in 5 secs
//                    msgCFP.setContent(subscription.getContent());    //TODO: intersection ID and contribution here? POJO?
//
//                    //negotiatorInit = new SectorJunctionNegotiatorInit(myAgent, msgCFP);
//                    addBehaviour(new SectorJunctionNegotiatorInit(myAgent, msgCFP));
//                }
//            });

return (Math.random() > 0.5);
}

}

class SectorProposalResp extends ProposeResponder {

    public SectorProposalResp(Agent a, MessageTemplate mt) {
        super(a, mt);
    }
    
    

    @Override
    protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {

        ACLMessage reply = propose.createReply();
        if (Math.random() > 0.5) {
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
        } else {
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
        }

        return reply;
    }
}

class SectorProposalInit extends ProposeInitiator {

    public SectorProposalInit(Agent a, ACLMessage msg) {
        super(a, msg);
    }

    @Override
    protected Vector prepareInitiations(ACLMessage propose) {

            // Retrieve the incoming subscription from the DataStore
        String incomingSubscriptionKey = (String) ((SectorSubscriptionResp) parent).SUBSCRIPTION_KEY;
        ACLMessage incomingSubscription = (ACLMessage) getDataStore().get(incomingSubscriptionKey);
            // Prepare the request to forward to the responder
        System.out.println("[S] " + getLocalName() + "\t *PROPOSE* register junction to sectors (x" + knownSectors.size() + ")");
        Vector v = new Vector(1);
        for (int s = 0; s < knownSectors.size(); s++) {
            ACLMessage outgoingPropose = new ACLMessage(ACLMessage.PROPOSE);
            outgoingPropose.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);
            outgoingPropose.addReceiver(knownSectors.get(s));
            outgoingPropose.setContent(incomingSubscription.getContent());
            outgoingPropose.setReplyByDate(incomingSubscription.getReplyByDate());
            v.addElement(outgoingPropose);
        }

        return v;
    }

    @Override
    protected void handleAcceptProposal(ACLMessage accept_proposal) {
            //super.handleAcceptProposal(accept_proposal); //To change body of generated methods, choose Tools | Templates.
            // TODO: send agree message to subscriber      
        storeNotification(ACLMessage.ACCEPT_PROPOSAL, accept_proposal);
    }

    @Override
    protected void handleRejectProposal(ACLMessage reject_proposal) {
            //super.handleRejectProposal(reject_proposal); //To change body of generated methods, choose Tools | Templates.
            // TODO: send refuse to subscriber
        storeNotification(ACLMessage.REJECT_PROPOSAL, reject_proposal);

    }

    @Override
    protected void handleNotUnderstood(ACLMessage notUnderstood) {
        storeNotification(ACLMessage.NOT_UNDERSTOOD,notUnderstood);
    }

//    @Override
//    protected void handleAllResponses(Vector responses) {
//        if (responses.size() == 0) {
//            storeNotification(ACLMessage.FAILURE, null);
//        }
//    }

    private void storeNotification(int performative, ACLMessage original) {
        if (performative == ACLMessage.ACCEPT_PROPOSAL) {
            System.out.println("Agent " + getLocalName() + ": proposal successful");
        } else {
            
            System.out.println("Agent " + getLocalName() + ": proposal failed");
        }

            // Retrieve the incoming request from the DataStore
        String incomingSubscriptionkey = (String) ((SectorSubscriptionResp) parent).SUBSCRIPTION_KEY;
        ACLMessage incomingSubscription = (ACLMessage) getDataStore().get(incomingSubscriptionkey);
            // Prepare the notification to the request originator and store it in the DataStore
        ACLMessage notification = incomingSubscription.createReply();
        notification.setPerformative(performative);
        if (original !=null)
            notification.setContent(original.getContent());
        String notificationkey = (String) ((SectorSubscriptionResp) parent).RESPONSE_KEY;
        getDataStore().put(notificationkey, notification);
    }
}

    String timestamp() {
        StringBuilder sb = new StringBuilder();
        Formatter format = new Formatter(sb, Locale.ENGLISH);
        format.format("%tT ", Calendar.getInstance());
        return sb.toString();
    }
}
