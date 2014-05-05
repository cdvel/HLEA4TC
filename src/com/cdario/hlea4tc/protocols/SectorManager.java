package com.cdario.hlea4tc.protocols;

import jade.core.AID;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.proto.SubscriptionInitiator;
import java.util.ArrayList;

/**
 * Manages the subscription to the DF and updates agent's known sectors
 */
public class SectorManager extends SubscriptionInitiator{

    ArrayList<AID> knownSectors;
    
    /**
     *
     * @param a
     * @param msg
     * @param kSectors
     */
    public SectorManager(Agent a, ACLMessage msg, ArrayList<AID> kSectors) {
        super(a, msg);
        knownSectors = kSectors;
    }

    /**
     *
     * @param inform
     */
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
                    System.out.println("DF> " + myAgent.getLocalName() + "\t # acknowledges " + knownSectors.size() + " sector(s)");
                } catch (FIPAException fe) {
                    fe.printStackTrace();
                }
    }
}
