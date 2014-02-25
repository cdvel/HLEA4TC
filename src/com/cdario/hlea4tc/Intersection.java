package com.cdario.hlea4tc;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.lang.acl.ACLMessage;
import java.awt.Color;


/**
 *
 * @author cdario
 */
public class Intersection extends Agent {
    
    private IntersectionGUI interGUI;
    public double remainingGreen;
    private int currentPhaseIndex;
    private double delay;
    private int[] queues;
    static boolean requestControl;
    
    ACLMessage lastMessage;

    @Override
    protected void setup() {
        remainingGreen = 100;
        
        //Create and show GUI
        interGUI = new IntersectionGUI(this);
        interGUI.showGui();
        interGUI.setVisible(true);
        
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                remainingGreen  --;
                interGUI.repaint();
            }
        } );
        
        System.out.println("Agent " + this.getLocalName() + " online");
    }
}