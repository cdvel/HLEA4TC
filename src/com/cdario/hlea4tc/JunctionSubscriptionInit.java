/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdario.hlea4tc;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

class JunctionSubscriptionInit extends SubscriptionInitiator {
        
        ArrayList<AID> knownSectors;
        AID mySector;
                
        JunctionSubscriptionInit(JunctionAgent agent) { 
            
            super(agent, new ACLMessage(ACLMessage.SUBSCRIBE));
            knownSectors = agent.getKnownSectors();
            mySector = agent.getMySector();
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
        @Override
        protected void handleAllResponses(Vector responses) {
            
            //TODO: discriminate responses
            System.out.println("[J] " + myAgent.getLocalName() + "\t <----------- NOTIFIED" );  
            for (int i=0; i<responses.size(); i++)
            {
                ACLMessage thisMsg = (ACLMessage)responses.get(i);
                if (thisMsg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
                {
                  System.out.println("[J] " + myAgent.getLocalName() + "\t ! SUBSCRIBED to " + thisMsg.getSender().getLocalName());  
                }
                
                
                // THIS IF...
                if (thisMsg.getPerformative() == ACLMessage.INFORM)
                    
                {
                  System.out.println("[J] " + myAgent.getLocalName() + "\t & INFORMED " + thisMsg.getContent());  
                }
            }
        }
        
        

        @Override
        protected void handleRefuse(ACLMessage refuse) {
            System.out.println("Junction " + myAgent.getLocalName() + ": Sector " + refuse.getSender().getLocalName() + " refused " + myAgent.getLocalName() + "'s subscription");
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
                    System.out.println("Junction " +myAgent.getLocalName() + ": Stays in Sector " + agree.getSender().getLocalName());
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