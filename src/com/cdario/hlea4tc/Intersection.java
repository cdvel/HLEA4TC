package com.cdario.hlea4tc;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.awt.Color;


/**
 *
 * @author cdario
 */
public class Intersection extends Agent {
    
    private IntersectionGUI interGUI;
    private int currentPhaseIndex;
    private double delay;
    private int[] queues;
    static boolean requestControl;
    
    ACLMessage lastMessage;

    @Override
    protected void setup() {
        interGUI = new IntersectionGUI();
        addBehaviour(new IntersectionStatus());
        interGUI.setVisible(true);
    }

 public class IntersectionStatus extends CyclicBehaviour
{
    @Override
    public void action() {
        lastMessage = receive();
        if(lastMessage!=null)
        {
            interGUI.button1.setForeground(Color.red);
            interGUI.button1.setText(lastMessage.getContent());
            lastMessage = null;
        }
        
        if (Intersection.requestControl = true)
        {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("Sector",AID.ISLOCALNAME));
            msg.setLanguage("English");
            msg.setOntology("send message-ontology");
            msg.setContent("Intersection requests control!");
            send(msg);
             //   System.out.println("Sent");
            Intersection.requestControl = false;
        }
    }
    
}

}


