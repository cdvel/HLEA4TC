/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.cdario.hlea4tc.protocols;

import com.cdario.hlea4tc.agents.SectorAgent;
import jade.core.Agent;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ProposeResponder;

public class SectorProposalResp extends ProposeResponder {

    public SectorProposalResp(SectorAgent a, MessageTemplate mt) {
        super(a, mt);
    }
    
    @Override
    protected ACLMessage prepareResponse(ACLMessage propose) throws NotUnderstoodException, RefuseException {

        ACLMessage reply = propose.createReply();
        if (Math.random() > 0.5) {
            reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
//            System.out.println("[S] " + myAgent.getLocalName() + " I reject " +propose.getContent()+" -> "+propose.getSender().getLocalName());
            System.out.println("[S] " + myAgent.getLocalName() + "\t << REJECT-PROPOSAL for -"+propose.getContent()+"- from "+propose.getSender().getLocalName());
            
        } else {
            reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
//            System.out.println("[S] " + myAgent.getLocalName() + " I accept "+propose.getContent()+" -> "+propose.getSender().getLocalName());
            System.out.println("[S] " + myAgent.getLocalName() + "\t << ACCEPT-PROPOSAL for -"+propose.getContent()+"- from "+propose.getSender().getLocalName());
        }
        reply.setContent(propose.getContent());

        return reply;
    }

}