package com.cdario.hlea4tc;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.util.Calendar;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;



/**
 *
 * @author cdario
 */
public class Intersection extends Agent {

    private IntersectionGUI interGUI;
    private AID[] availableSectors;
    private AID mySector;
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
        try {
            DFService.register(this, dfa);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }

        // update available sectors list every 5 secs
        
        addBehaviour(new TickerBehaviour(this, 5000) {  // look for sectors every 5 secs?
            @Override
            protected void onTick() {
                
            }
        });
        
        addBehaviour(new SectorManager(this, 20000, new Date())); // every 20s
        addBehaviour(new GreenUpdater(this, 1000));
        addBehaviour(new SectorReporter(this, 2000));
        System.out.println("I-Agent " + this.getLocalName() + " online");
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        interGUI.dispose();
        System.out.println("intersection-agent " + getAID().getName() + " terminating");
    }
    
    private class SectorManager extends TickerBehaviour
    {
        private long deadline, initTime, deltaT;

        public SectorManager(Agent a, long period, Date d) {
            super(a, period);
            deadline = d.getTime();
            initTime = System.currentTimeMillis();
            deltaT = deadline - initTime;
        }

        @Override
        protected void onTick() {
            
            /******************************/
            /*      update list of available sectors    */
            /*******************************/
            DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("sector-registration");
                template.addServices(sd);
                try {
                    DFAgentDescription[] res = DFService.search(myAgent, template);
                    availableSectors = new AID[res.length];
                    for (int i = 0; i < res.length; i++) {
                        availableSectors[i] = res[i].getName();
                    }
                } catch (FIPAException e) {
                    e.printStackTrace();
                }
                System.out.println(timestamp()+" ["+myAgent.getAID().getLocalName()+"] knows"+ availableSectors.length +" sectors");
            /*
             * end update
             */
            
            long currentTime = System.currentTimeMillis();
//            if (false){
//            //if (currentTime > deadline){  //TODO: check for deadlines
//                //deadline expired
//                System.out.println(myAgent.getAID()+ " deadline reached - did not join sector");
//                stop();
//            }
//            else
//            {
                //negotiate
                myAgent.addBehaviour(new SectorReallocationRequester(this));
//            }
        }
    }

    private class SectorReallocationRequester extends Behaviour {

        int step = 0;
        private MessageTemplate mtemplate;
        private AID mSector;
        private int mSectorContribution;
        private int repliesCount = 0;
        private SectorManager manager;

        public SectorReallocationRequester(SectorManager mng) {
            super(null);
            manager = mng;
        }
     
        @Override
        public void action() {
            switch (step) {
                case 0: //Call for proposal for all sectors
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for (int i = 0; i < availableSectors.length; i++) {
                        cfp.addReceiver(availableSectors[i]);
                    }
                    cfp.setContent("intersection-state-info-and-id");
                    cfp.setConversationId("request-join-sector");
                    cfp.setReplyWith("join-cfp-" + System.currentTimeMillis()); //i dentifier
                    //cfp.setReplyByDate(System.currentTimeMillis()+1000);
                    cfp.setReplyByDate(new Date());
                    myAgent.send(cfp);
                    //proposal templates to come
                    mtemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("request-join-sector"), MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                    System.out.println(timestamp()+" ["+myAgent.getAID().getLocalName()+"] action SEND CFP ALL SECTORS");
                    //mtemplate = MessageTemplate.and(mtemplate, MessageTemplate.MatchReplyByDate(new Date()));
                    step = 1;
                    break;

                case 1: //receive all replies from sectors
                    ACLMessage reply = myAgent.receive(mtemplate);
                    if (reply != null) {
                        if (reply.getPerformative() == ACLMessage.PROPOSE) {  
                            // requesting join 
                            int contribution = Integer.parseInt(reply.getContent());
                            //TODO: maintan value and compare with other offers, keep the best next step
                            if (mSector == null || contribution < mSectorContribution) { 
                                mSector = reply.getSender();
                            }
                            mSectorContribution = contribution;

                        }
                        repliesCount++;
                        if (repliesCount >= availableSectors.length) {
                             //TODO: define deadline
                            step = 2; //received all, proceed
                        }
                        System.out.println(timestamp()+" ["+myAgent.getAID().getLocalName()+"] action RECEIVE "+repliesCount +" PROPOSALS");
                    } else {
                        block();
                    }
                    

                    break;
                
                case 2: 
                    if (mSector !=null) //TODO: more guards
                    {
                        // accept the offer from the chosen sector 
                        ACLMessage acceptMessage = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                        acceptMessage.addReceiver(mSector);
                        acceptMessage.setContent("intersection-state-info-and-id");
                        acceptMessage.setConversationId("request-join-sector");
                        acceptMessage.setReplyWith("accept-cfp"+System.currentTimeMillis());
                        myAgent.send(acceptMessage);
                        //template
                        mtemplate = MessageTemplate.and(MessageTemplate.MatchConversationId("request-join-sector"), MessageTemplate.MatchInReplyTo(acceptMessage.getReplyWith())); 
                        step = 3;
                    }else{
                        // no sector wants this intersection to join
                        step = 4;
                    }
                    
                    System.out.println(timestamp()+" ["+myAgent.getAID().getLocalName()+"] action SEND ACCEPT OFFERS");
                    break;
                    
                case 3:
                    // receive the reply when joining the sector
                    reply = myAgent.receive(mtemplate);
                    
                    if(reply != null)
                    {
                        //received reply from join
                        if(reply.getPerformative() == ACLMessage.INFORM)
                        {
                            //joining this sector, end
                            System.out.println(myAgent.getAID()+" joined "+mSector);
                            
                            manager.stop();
                        }
                        System.out.println(timestamp()+" ["+myAgent.getAID().getLocalName()+"] action RECEIVE CONFIRMATION");
                        step = 4;
                    }else{
                        block();
                    }
                    break;
   
            }

            //System.out.println(timestamp()+" Sector reallocation - step "+step);
        }

        @Override
        public boolean done() {
            return step == 4;
        }
    } // end of inner class SectorReallocation

    private class SectorReporter extends TickerBehaviour { // executed constantly 

        public SectorReporter(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            
            if (availableSectors != null)
            {
                ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                for (int i=0; i<availableSectors.length; i++)
                {
                    msg.addReceiver(availableSectors[i]);
                    //msg.addReceiver(new AID("Sector1", AID.ISLOCALNAME));// TODO
                }

                msg.setLanguage("EN");
                msg.setOntology("Traffic-control-ontology");
                msg.setContent(remainingGreen + "-sec left");

                send(msg);
                System.out.println(timestamp()+" ["+myAgent.getAID().getLocalName()+"] is reporting");
            }
        }
    }

    private class GreenUpdater extends TickerBehaviour {

        public GreenUpdater(Agent a, long period) {
            super(a, period);
        }

        @Override
        protected void onTick() {
            if (remainingGreen == 0) {
                remainingGreen = MIN_GREEN;
            } else {
                remainingGreen--;
            }
            interGUI.repaint();
        }
    }

String timestamp()
{
    Date d = new Date();
    StringBuilder sb = new StringBuilder();
    Formatter format = new Formatter(sb, Locale.ENGLISH);
    format.format("%tT ",Calendar.getInstance() );
    return sb.toString();
}
}// end of outter Class

