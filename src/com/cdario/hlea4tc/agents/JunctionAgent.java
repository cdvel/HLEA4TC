package com.cdario.hlea4tc.agents;

import com.cdario.hlea4tc.protocols.JunctionSubscriptionInit;
import com.cdario.hlea4tc.protocols.SectorManager;
import com.cdario.hlea4tc.integration.ControllerNativeInterface;
import com.cdario.hlea4tc.integration.PlatformMediator;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;


public class JunctionAgent extends Agent {

    protected String junctionID;
    protected AID mySector;
    protected ArrayList<AID> knownSectors;
    protected ControllerNativeInterface controller;

    private PlatformMediator app = PlatformMediator.getInstance();
    /**
     *
     */
    @Override
    protected void setup() {

        knownSectors = new ArrayList<AID>();
        junctionID = getLocalName();
        setEnabledO2ACommunication(true, AP_MAX);    // permits interactions from Mediator
        //controller = new ControllerNativeInterface();
        //String greeting = ControllerNativeInterface.callControllerWrapper("j-01", 12);
        //System.out.println(">>>> "+greeting);

        /*
         *      Behaviour: update known sectors by subscribing to DF
         */

        DFAgentDescription description = new DFAgentDescription();
        ServiceDescription sdd = new ServiceDescription();
        sdd.setName("sector-registration");
        sdd.setType("DF-Subscriptions");
        description.addServices(sdd);
        
        // subscribe to new sectors dynamically
        SectorManager sectorUpdater = new SectorManager(this, DFService.createSubscriptionMessage(this, getDefaultDF(), description, null), knownSectors);
        addBehaviour(sectorUpdater);

        /*
         * attempt subscribe every 5 seconds
         */

        
//        final JunctionSubscriptionInit initSub = new JunctionSubscriptionInit(this);
//        addBehaviour(initSub);
        
        addBehaviour(new TickerBehaviour(this, 8000) {
            @Override
            protected void onTick() {
                myAgent.addBehaviour(new JunctionSubscriptionInit((JunctionAgent)myAgent));
            }
        });
        
        
//        addBehaviour(new CyclicBehaviour(this){
//            @Override
//            public void action() {
//                JunctionUpdateBean update = (JunctionUpdateBean)myAgent.getO2AObject();
//                if (update!=null)
//                {
//                    //do something
//                    //update.getJunctionID(); update.getValue(); update.getTimestamp();
//                }else{
//                    block();
//                }
//            }
//        });
        
        
//        addBehaviour(new TickerBehaviour(this, 8000) {
//            @Override
//            protected void onTick() {
//                JunctionSubscriptionInit initSub = new JunctionSubscriptionInit((JunctionAgent)myAgent);
////                initSub.registerHandleAllResponses(new AchieveREResponder(myAgent, null){
////
////                    @Override
////                    protected ACLMessage prepareResponse(ACLMessage request) throws NotUnderstoodException, RefuseException {
////                        
////                        System.out.println(">--------> "+ request.getContent());
////                        return request.createReply();
////                    }
////                });
//                
//                if (knownSectors != null && knownSectors.size() > 0) {
//                    addBehaviour(initSub);
//                } else {
//                    System.out.println("No responder specified.");
//                }
//            }
//        });
    } 
    
    public ArrayList<AID> getKnownSectors(){
        return knownSectors;
    }
    
    public AID getMySector ()
    {
        return mySector;
    }
    
}
