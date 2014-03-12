/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdario.hlea4tc;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.AchieveREResponder;
import jade.proto.SubscriptionResponder;
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

    @Override
    protected void setup() {
        System.out.println("Agent " + getLocalName() + " waiting for subscription requests...");
        addBehaviour(new SectorSubscriptionResp(this));
    }

    class SectorSubscriptionResp extends SubscriptionResponder {

        //TODO: check other constructor with SubscriptionManager
        SectorSubscriptionResp(Agent a) {
            super(a, MessageTemplate.and(
                    MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.SUBSCRIBE),
                    MessageTemplate.MatchPerformative(ACLMessage.CANCEL)),
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_SUBSCRIBE)));
        }

        @Override
        protected ACLMessage handleSubscription(ACLMessage subscription) throws RefuseException {
            // handle a subscription request
            
            createSubscription(subscription);
            
             System.out.println("Agent " + getLocalName() + ": SUBSCRIPTION received from " + subscription.getSender().getName() + ". Action is " + subscription.getContent());
            if (checkAction()) {
                // We agree to perform the action. 
                System.out.println("Agent " + getLocalName() + ": Agree");
                ACLMessage agree = subscription.createReply();
                agree.setPerformative(ACLMessage.AGREE);
                return agree;
            } else {
                // We refuse to perform the action
                System.out.println("Agent " + getLocalName() + ": Refuse");
                ACLMessage refuse = subscription.createReply();
                refuse.setPerformative(ACLMessage.REFUSE);
                return refuse;
            }
              // if successful, should answer (return) with AGREE; otherwise with REFUSE or NOT_UNDERSTOOD
            //TODO: implemend return for not-understood
            
            // notifyJunctions(subscription_msg);

        }



        protected void notifyJunctions(ACLMessage inform) {
            // this is the method you invoke ("call-back") for creating a new inform message;
            // it is not part of the SubscriptionResponder API, so rename it as you like

            // go through every subscription
            Vector subs = getSubscriptions();
            for (int i = 0; i < subs.size(); i++) {
                ((SubscriptionResponder.Subscription) subs.elementAt(i)).notify(inform);
            }
        }
    }

    private boolean checkAction() {
        // Simulate a check by generating a random number
        return (Math.random() > 0.2);
    }

    private boolean performAction() {
        // Simulate action execution by generating a random number
        return (Math.random() > 0.2);
    }
}
