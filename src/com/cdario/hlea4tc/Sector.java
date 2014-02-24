package com.cdario.hlea4tc;

/**
 *
 * @author cdario
 */

import jade.core.Agent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.CyclicBehaviour;
import java.awt.Color;

public class Sector extends Agent{
    
    public int activeIntersection;
    ACLMessage message;
    SectorGUI sectorGUI;

    @Override
    protected void setup() {
        sectorGUI = new SectorGUI();
        addBehaviour(new SectorStatus());
        sectorGUI.setVisible(true);
    }
    
    public class SectorStatus extends CyclicBehaviour{

        @Override
        public void action() {
            
            message = receive();
            
            if (message !=null)
            {
                sectorGUI.label1.setForeground(Color.red);
                sectorGUI.label1.setText(message.getContent());
                message = null;
            }
            
        }
    
    }
    
    
}
