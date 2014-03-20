package com.cdario.hlea4tc;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.proto.AchieveREResponder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Formatter;
import java.util.Locale;

/*
 * Implements the initiator roles in FIPA-Subscribe IP.
 */
/**
 *
 * @author cesar
 */
public class JunctionAgent extends Agent {

    /**
     *
     */
    protected String junctionID;
    /**
     *
     */
    protected AID mySector;
    /**
     *
     */
    protected ArrayList<AID> knownSectors;

    /**
     *
     */
    @Override
    protected void setup() {

        knownSectors = new ArrayList<AID>();
        junctionID = getLocalName();

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

    String timestamp() {
        StringBuilder sb = new StringBuilder();
        Formatter format = new Formatter(sb, Locale.ENGLISH);
        format.format("%tT ", Calendar.getInstance());
        return sb.toString();
    }
    
    ArrayList<AID> getKnownSectors(){
        return knownSectors;
    }
    
    AID getMySector ()
    {
        return mySector;
    }
    
}
