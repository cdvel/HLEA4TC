package com.cdario.hlea4tc;

/**
 *
 * @author cdario
 */
import com.sun.xml.internal.ws.api.server.ServiceDefinition;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.awt.Color;
import java.awt.Component;

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
                sectorGUI.labelMsg.setForeground(Color.red);
                /*
                 String current = sectorGUI.labelMsg.getText();
                 if (current.length() > 50) {
                 sectorGUI.labelMsg.setText("");
                 }
                 sectorGUI.labelMsg.setText(current + "\n" + message.getSender().getLocalName() + " says " + message.getContent());
                 //*/
                sectorGUI.labelMsg.setText(message.getSender().getLocalName() + " says " + message.getContent());
                message = null;
            } else {
                block();    // good practice
            }
        }
    }
}
