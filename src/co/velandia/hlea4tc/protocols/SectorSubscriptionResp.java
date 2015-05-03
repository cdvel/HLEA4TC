/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package co.velandia.hlea4tc.protocols;

import co.velandia.hlea4tc.agents.SectorAgent;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.SubscriptionResponder;
import java.util.Vector;

/**
 *
 * Manages incoming subscription from junctions to sectors
 *
 */
public class SectorSubscriptionResp extends SubscriptionResponder {

    //TODO: check other constructor with SubscriptionManager
    public SectorSubscriptionResp(SectorAgent a) {
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

        System.out.println(myAgent.getLocalName() + " : Canceling subscription from " + cancel.getSender().getLocalName());
        Subscription subsc = getSubscription(cancel);
        //           if (subsc!=null)    //TODO: subscription 
        //             this.mySubscriptionManager.deregister(subsc);
        return null;             //return cancel;

    }

    /*
     * Note: If registerhandlesubscription is used; handleSubscription won't work
     */
    @Override
    protected ACLMessage handleSubscription(ACLMessage subscriptionMsg) throws RefuseException {
        System.out.println(myAgent.getLocalName() + ": Received subscription request from " + subscriptionMsg.getSender().getLocalName() + " [" + subscriptionMsg.getContent() + "]");
        ACLMessage reply = subscriptionMsg.createReply();

        // if successful, should answer (return) with AGREE; otherwise with REFUSE or NOT_UNDERSTOOD
        //TODO: implemend return for not-understood

        if (junctionMatches(subscriptionMsg)) {
            /* negotiate (propose) with other sectors here */
            if (junctionAwarded(subscriptionMsg)) {
                // We agree to perform the action. 
                Vector previousSub = getSubscriptions(subscriptionMsg.getSender());   // 
                if (previousSub.size() == 0) // one subscriptionMsg per junction
                {
                    createSubscription(subscriptionMsg);
                }
                System.out.println(myAgent.getLocalName() + ": AGREED subscription request from " + subscriptionMsg.getSender().getLocalName() + " [" + subscriptionMsg.getContent() + "]");
                reply.setPerformative(ACLMessage.AGREE);

            } else {
                reply.setPerformative(ACLMessage.REFUSE);
            }
        } else {
            // We refuse to perform the action
            System.out.println(myAgent.getLocalName() + ": REFUSED subscription request from " + subscriptionMsg.getSender().getLocalName() + " [" + subscriptionMsg.getContent() + "]");
            reply.setPerformative(ACLMessage.REFUSE);
        }

        storeNotification(ACLMessage.SUBSCRIBE);
        return reply;
    }

    private void storeNotification(int performative) {
        if (performative == ACLMessage.SUBSCRIBE) {
            System.out.println("Agent " + myAgent.getLocalName() + ": subscription successful");
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

    public void notifyJunctions(ACLMessage inform) {
        // this is the method you invoke ("call-back") for creating a new inform message; not part of the SubscriptionResponder API, rename it

        // go through every subscription
        Vector subs = getSubscriptions(); // from stored by createSubscription
        for (int i = 0; i < subs.size(); i++) {
            ((SubscriptionResponder.Subscription) subs.elementAt(i)).notify(inform);
        }
    }

    private boolean junctionMatches(ACLMessage subscription) {
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