/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdario.hlea4tc;

import jade.core.Agent;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.proto.SubscriptionResponder;
import jade.proto.SubscriptionResponder.SubscriptionManager;

/**
 *
 * @author cesar
 */
public class MediatorAgent extends Agent{

    public MediatorAgent() {
    }
        
    class SectorSubscriptionManager implements SubscriptionManager{

        @Override
        public boolean register(SubscriptionResponder.Subscription s) throws RefuseException, NotUnderstoodException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public boolean deregister(SubscriptionResponder.Subscription s) throws FailureException {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
