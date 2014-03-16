/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import jade.proto.SubscriptionInitiator;
import jade.proto.SubscriptionResponder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Locale;
import java.util.Vector;

/**
 *
 * @author cesar
 */

/*
 * Implements the responder role in FIPA-Subscribe IP. 
 */
public class SectorAgent extends Agent {

    protected int sectorID;
    protected AID[] myJunctions;
    //protected AID[] knownSectors;
    protected ArrayList<AID> knownSectors;
    protected SectorSubscriptionResp responder;
    protected SectorJunctionNegotiatorInit negotiatorInit;
    protected SectorJunctionNegotiatorResp negotiatorResp;

    @Override
    protected void setup() {
        
        knownSectors = new ArrayList<AID>();
        
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
                    System.out.println( getLocalName() + ": "+knownSectors.size()+" sector(s) known");
		}
		catch (FIPAException fe) {
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
        
        System.out.println(getLocalName() + " waiting for subscription requests...");
        //SubscriptionResponder.SubscriptionManager manager = new SubscriptionResponder(this, null).
        responder = new SectorSubscriptionResp(this);
        addBehaviour(responder);

        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
//                AID senderID = ((SubscriptionResponder.Subscription) responder.getSubscriptions().get(0)).getMessage().getSender();
               
                inform.setContent(timestamp()+" adjust x secs ("+responder.getSubscriptions().size()+")");
                //if (responder.getSubscriptions().size()>0)
                //inform.setContent(timestamp()+" == "+((SubscriptionResponder.Subscription)responder.getSubscriptions().get(0)).getMessage().getSender());
                //TODO: content is junction-specific?
                
                responder.notifyJunctions(inform);
            }
        });

        /*
         *      Behaviour: negotiate junctions once? every 10 seconds?
         */
        
        addBehaviour(new WakerBehaviour(this, 10000) {

            @Override
            protected void onWake() {
               
            MessageTemplate contractTemplate = MessageTemplate.and(
            MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
            MessageTemplate.MatchPerformative(ACLMessage.CFP));

            ACLMessage msgCFP = new ACLMessage(ACLMessage.CFP);
            for (int i = 0; i < knownSectors.size(); ++i) {
                msgCFP.addReceiver(new AID((String) knownSectors.get(i).getLocalName(), AID.ISLOCALNAME));
            }

            msgCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
            msgCFP.setReplyByDate(new Date(System.currentTimeMillis() + 10000)); // We want to receive a reply in 10 secs
            msgCFP.setContent("sector-negotiation");    //TODO: intersection ID and contribution here?

            negotiatorInit = new SectorJunctionNegotiatorInit(myAgent, msgCFP);
            negotiatorResp = new SectorJunctionNegotiatorResp(myAgent, contractTemplate);

            addBehaviour(negotiatorInit);
            addBehaviour(negotiatorResp);
                
            }
        });
    }

    class SectorSubscriptionResp extends SubscriptionResponder {

        //TODO: check other constructor with SubscriptionManager
        
        SectorSubscriptionResp(Agent a) {
            super(a, MessageTemplate.and(
                    MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
                    MessageTemplate.MatchPerformative(ACLMessage.CANCEL)),
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)));
        }

        // USE to nest?
        //        @Override
        //        public void registerHandleSubscription(Behaviour b) {
        //            super.registerHandleSubscription(b); //To change body of generated methods, choose Tools | Templates.
        //        }
        @Override
        protected ACLMessage handleCancel(ACLMessage cancel) throws FailureException {
            
            System.out.println(getLocalName()+" : Canceling subscription from "+cancel.getSender().getLocalName());
            Subscription subsc = getSubscription(cancel);
 //           if (subsc!=null)    //TODO: subscription 
//                this.mySubscriptionManager.deregister(subsc);
            //return cancel;
            return null;
            //return super.handleCancel(cancel); //To change body of generated methods, choose Tools | Templates.
        }
        
        @Override
        protected ACLMessage handleSubscription(ACLMessage subscriptionMsg) throws RefuseException {
            // handle a subscriptionMsg request
      
            System.out.println(getLocalName() + ": Received subscription request from " + subscriptionMsg.getSender().getLocalName() + " [" + subscriptionMsg.getContent()+"]");
            ACLMessage reply = subscriptionMsg.createReply();

            // if successful, should answer (return) with AGREE; otherwise with REFUSE or NOT_UNDERSTOOD
            //TODO: implemend return for not-understood

            if (junctionFits(subscriptionMsg)) {
                /* negotiate (Contract Net) with other sectors here */
                if (junctionAwarded(subscriptionMsg)) {
                    // We agree to perform the action. 
                     Vector previousSub = getSubscriptions(subscriptionMsg.getSender());   // 
                     if (previousSub.size()==0) // one subscriptionMsg per junction
                        createSubscription(subscriptionMsg);
                    System.out.println(getLocalName() + ": AGREED subscription request from " + subscriptionMsg.getSender().getLocalName() + " [" + subscriptionMsg.getContent()+"]");
                    reply.setPerformative(ACLMessage.AGREE);

                } else {
                    reply.setPerformative(ACLMessage.REFUSE);
                }
            } else {
                // We refuse to perform the action
                System.out.println(getLocalName() + ": REFUSED subscription request from " + subscriptionMsg.getSender().getLocalName() + " [" + subscriptionMsg.getContent()+"]");
                reply.setPerformative(ACLMessage.REFUSE);
            }
            return reply;
            // notifyJunctions(subscription_msg);
        }

        protected void notifyJunctions(ACLMessage inform) {
            // this is the method you invoke ("call-back") for creating a new inform message;
            // it is not part of the SubscriptionResponder API, so rename it as you like

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
            addBehaviour(new OneShotBehaviour(myAgent) {
                
                @Override
                public void action() {
                    /*
                     * send CFPs to ll known sectors
                     */
                    ACLMessage msgCFP = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < knownSectors.size(); ++i) {
                        msgCFP.addReceiver(new AID((String) knownSectors.get(i).getLocalName(), AID.ISLOCALNAME));
                    }

                    msgCFP.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    msgCFP.setReplyByDate(new Date(System.currentTimeMillis() + 5000)); // We want to receive a reply in 5 secs
                    msgCFP.setContent(subscription.getContent());    //TODO: intersection ID and contribution here? POJO?

                    //negotiatorInit = new SectorJunctionNegotiatorInit(myAgent, msgCFP);
                    addBehaviour(new SectorJunctionNegotiatorInit(myAgent, msgCFP));
                }
            });
  
            return (Math.random() > 0.5);
        }
    }

    class SectorJunctionNegotiatorInit extends ContractNetInitiator {

        public SectorJunctionNegotiatorInit(Agent a, ACLMessage cfp) {
            super(a, cfp);
        }

        @Override
        protected void handlePropose(ACLMessage propose, Vector acceptances) {
            
 //           System.out.println("Sector: Agent " + propose.getSender().getLocalName() + " proposed " + propose.getContent());
        }

        @Override
        protected void handleRefuse(ACLMessage refuse) {
 //           System.out.println("Sector: Agent " + refuse.getSender().getLocalName() + " refused");
        }

        @Override
        protected void handleFailure(ACLMessage failure) {
            if (failure.getSender().equals(myAgent.getAMS())) {
                // FAILURE notification from the JADE runtime: the receiver
                // does not exist
                System.out.println("Responder does not exist");
            } else {
                System.out.println(getLocalName()+" : Failed subscription from "+failure.getSender().getLocalName());
            }
            // Immediate failure --> we will not receive a response from this agent
            //	nResponders--;
        }

        @Override
        protected void handleAllResponses(Vector responses, Vector acceptances) {
            if (responses.size() < knownSectors.size()) {
                // Some responder didn't reply within the specified timeout
                System.out.println(getLocalName()+" : Timeout expired: missing " + (knownSectors.size() - responses.size()) + " responses");
            }
            // Evaluate proposals.
            int bestProposal = -1;
            AID bestProposer = null;
            ACLMessage accept = null;
            Enumeration e = responses.elements();
            while (e.hasMoreElements()) {
                ACLMessage msg = (ACLMessage) e.nextElement();
                if (msg.getPerformative() == ACLMessage.PROPOSE) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    acceptances.addElement(reply);
                    int proposal = Integer.parseInt(msg.getContent());  //TODO: see getContentObject
                    if (proposal > bestProposal) {
                        bestProposal = proposal;
                        bestProposer = msg.getSender();
                        accept = reply;
                    }
                }
            }
            // Accept the proposal of the best proposer
            if (accept != null) {
 //               System.out.println("Sector "+myAgent.getLocalName()+": Accepting proposal " + bestProposal + " from responder " + bestProposer.getLocalName());
                accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }
        }

        @Override
        protected void handleInform(ACLMessage inform) {
 //           System.out.println("Agent " + inform.getSender().getLocalName() + " successfully performed the requested action");
        }
    }

    class SectorJunctionNegotiatorResp extends ContractNetResponder {

        public SectorJunctionNegotiatorResp(Agent a, MessageTemplate mt) {
            super(a, mt);
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
            System.out.println(getLocalName() + " : CFP received from " + cfp.getSender().getLocalName()+ " [" + cfp.getContent()+"]");
            int proposal = evaluateAction();
            if (proposal > 2) {
                // We provide a proposal
//                System.out.println("Sector " + getLocalName() + ": Proposing " + proposal);
                ACLMessage propose = cfp.createReply();
                propose.setPerformative(ACLMessage.PROPOSE);
                propose.setContent(String.valueOf(proposal));
                return propose;
            } else {
                // We refuse to provide a proposal
//                System.out.println("Sector " + getLocalName() + ": Refuse");
                throw new RefuseException("evaluation-failed");
            }
        }

        @Override
        protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept) throws FailureException {
            System.out.println(getLocalName() + " : Proposal accepted");
            if (performAction()) {
                System.out.println(getLocalName() + ": Action successfully performed");
                ACLMessage inform = accept.createReply();
                inform.setPerformative(ACLMessage.INFORM);
                return inform;
            } else {
                System.out.println(getLocalName() + ": Action execution failed");
                throw new FailureException("unexpected-error");
            }
        }

        protected void handleRejectProposal(ACLMessage reject) {
 //           System.out.println("Sector " + getLocalName() + ": Proposal rejected");
        }

       private int evaluateAction() {
  	// Simulate an evaluation by generating a random number
  	return (int) (Math.random() * 10);
  }
  
  private boolean performAction() {
  	// Simulate action execution by generating a random number
  	return (Math.random() > 0.2);
  }
    }
    //    private boolean checkAction() {
//        // Simulate a check by generating a random number
//        return (Math.random() > 0.2);
//    }

    //TODO: reuse this class here and junction
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
//            /*  update list of available sectors */
//            DFAgentDescription template = new DFAgentDescription();
//            ServiceDescription sd = new ServiceDescription();
//            sd.setType("sector-registration");
//            template.addServices(sd);
//            try {
//                DFAgentDescription[] res = DFService.search(myAgent, template);
//                //knownSectors = new AID[res.length];
//                for (int i = 0; i < res.length; i++) {
//                    if (res[i].getName() != myAgent.getAID())   // know thyself
//                        //knownSectors[i] = res[i].getName();
//                        knownSectors.add(res[i].getName());
//                }
//            } catch (FIPAException e) {
//                e.printStackTrace();
//            }
//            //System.out.println(timestamp() + " [" + myAgent.getAID().getLocalName() + "] knows " + knownSectors.length + " sector(s)");
//        }
//    }

     String timestamp() {
        StringBuilder sb = new StringBuilder();
        Formatter format = new Formatter(sb, Locale.ENGLISH);
        format.format("%tT ", Calendar.getInstance());
        return sb.toString();
    }
}
