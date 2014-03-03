package com.cdario.hlea4tc;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Color;




/**
 *
 * @author cdario
 */
public class Intersection extends Agent {
    
    private IntersectionGUI interGUI;
    private AID[] availableSectors;
    
    private static int MIN_GREEN = 90;
        
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
        
        //registering service
        DFAgentDescription dfa = new DFAgentDescription();
        dfa.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("status-reporting");
        sd.setName("HLEA4TC");
        dfa.addServices(sd);
        try{
            DFService.register(this, dfa);
        }
        catch(FIPAException fe){
            fe.printStackTrace();
        }
        
        addBehaviour(new TickerBehaviour(this, 10000) {  // look for sectors every 10 secs?
            @Override
            protected void onTick() {
                DFAgentDescription template =  new DFAgentDescription();
                ServiceDescription sd  = new ServiceDescription();
                sd.setType("sector-registration");
                template.addServices(sd);
                try {
                    DFAgentDescription[] res =  DFService.search(myAgent, template);
                    availableSectors = new AID[res.length];
                    for (int i=0;i<res.length;i++){
                        availableSectors[i] = res[i].getName();
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
                                
                myAgent.addBehaviour(new SectorReallocationRequester());
            }
        });
        
        addBehaviour(new GreenUpdater());
        
        addBehaviour(new ReportState2Sector());
        
        System.out.println("Agent " + this.getLocalName() + " online");
    }

    @Override
    protected void takeDown() {
        try{
            DFService.deregister(this);
        }
        catch (FIPAException fe){
            fe.printStackTrace();
        }
        interGUI.dispose();
        System.out.println("intersection-agent "+getAID().getName()+" terminating.");
    }
    
    
    private class SectorReallocationRequester extends OneShotBehaviour{

        @Override
        public void action() {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
        
    }
    
    private class ReportState2Sector extends CyclicBehaviour{

        @Override
        public void action() {
            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
            msg.addReceiver(new AID("Sector1", AID.ISLOCALNAME));// TODO
            msg.setLanguage("English");
            msg.setOntology("Traffic-control-ontology");
            msg.setContent(remainingGreen+"-sec left");
            send(msg);
        }
    }    
    
    private class GreenUpdater extends CyclicBehaviour{
        @Override
        public void action() {
            addBehaviour(new TickerBehaviour(myAgent, 1000) {
                @Override
                protected void onTick() {
                    if (remainingGreen == 0)
                        
                    remainingGreen = MIN_GREEN;
                else
                    remainingGreen  --;
                interGUI.repaint();
                }
            });
        }
    }
    
    
}// end of outter Class