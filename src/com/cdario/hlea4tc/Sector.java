package com.cdario.hlea4tc;

/**
 *
 * @author cdario
 */
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.awt.Color;


public class Sector extends Agent {

    public int activeIntersection;
    ACLMessage message;
    SectorGUI sectorGUI;

    @Override
    protected void setup() {

        //register with DF
        DFAgentDescription dfad = new DFAgentDescription();
        dfad.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("sector-registration");
        sd.setName("HLE4TC");
        dfad.addServices(sd);
        try {
            DFService.register(this, dfad);
        } catch (Exception fe) {
            fe.printStackTrace();
        }

        sectorGUI = new SectorGUI();
        addBehaviour(new SectorStatus());
        sectorGUI.setVisible(true);
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (Exception fe) {
            fe.printStackTrace();
        }

        sectorGUI.dispose();
        System.out.println("sector-agent " + getAID().getName() + " terminating.");
    }

    public class SectorStatus extends CyclicBehaviour {

        @Override
        public void action() {

            message = receive();

            if (message != null) {
                
                if(message.getPerformative() == ACLMessage.INFORM)
                {
                    sectorGUI.labelMsg.setForeground(Color.red); // display
                    sectorGUI.labelMsg.setText(message.getSender().getLocalName() + " says " + message.getContent());
                    //System.out.println("Sector INFORMED");
                }
                else
                {
                    ACLMessage reply = message.createReply();

                    if (message.getPerformative() == ACLMessage.CFP)  // intersection requests join sector
                    {
                        //TODO: Default action accept all requests!
                        reply.setPerformative(ACLMessage.PROPOSE);
                        int contribution = 10; //TODO: compute
                        reply.setContent(""+contribution);
                        //System.out.println("Sector PROPOSED");
                    }else
                    {
                        if (message.getPerformative() == ACLMessage.ACCEPT_PROPOSAL)
                        {
                            reply.setPerformative(ACLMessage.CONFIRM);
                            reply.setContent("OK");
                            //System.out.println("Sector ACCEPTED");
                        }
                    }
                    myAgent.send(reply);
                }

                message = null;
            } else {
                block();    // good practice
            }
        }
    }
}
