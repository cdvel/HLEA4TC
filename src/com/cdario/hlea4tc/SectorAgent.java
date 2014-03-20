package com.cdario.hlea4tc;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.core.behaviours.WakerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import jade.proto.ProposeInitiator;
import jade.proto.ProposeResponder;
import jade.proto.SubscriptionInitiator;
import jade.proto.SubscriptionResponder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.Formatter;
import java.util.Locale;
import java.util.Vector;

/*
 * Implements the responder role in FIPA-Subscribe IP. 
 */
/**
 *
 * @author cesar
 */
public class SectorAgent extends Agent {

    /**
     *
     */
    protected int sectorID;
    /**
     *
     */
    protected AID[] myJunctions;
    //protected AID[] knownSectors;
    /**
     *
     */
    protected ArrayList<AID> knownSectors;
    private SectorSubscriptionResp subscriptionResponder;
    private SectorProposalInit proposalInitiator;
    private SectorProposalResp proposalResponder;

    /**
     *
     */
    @Override
    protected void setup() {

        knownSectors = new ArrayList<AID>();
        System.out.println("[S] " + getLocalName() + "\t + is up and waiting for subscriptions...");
        
        /*
         *      Register with DF and subscribe to sector-resgistration
         */

        DFAgentDescription description = new DFAgentDescription();
        ServiceDescription sdd = new ServiceDescription();
        sdd.setName("sector-registration");
        sdd.setType("DF-Subscriptions");
        description.addServices(sdd);
        
        // subscribe to new sectors dynamically
        SectorManager sectorUpdater = new SectorManager(this, DFService.createSubscriptionMessage(this, getDefaultDF(), description, null), knownSectors);
        addBehaviour(sectorUpdater);
        
        // and register itself to the DF
        try {
            DFService.register(this, description); 
        } catch (Exception fe) {
            fe.printStackTrace();
        }


        //SubscriptionResponder.SubscriptionManager manager = new SubscriptionResponder(this, null).
        subscriptionResponder = new SectorSubscriptionResp(this);
        proposalInitiator = new SectorProposalInit(this, null);  // msg is null; prepareInitiations() specifies initiator message 
        subscriptionResponder.registerHandleSubscription(proposalInitiator);
    //    proposalInitiator.registerHandleAllResponses(sectorUpdater);
        //LOOP?
        //propInitiator.registerHandleAllResponses(sectorUpdater);
        //proposalResponder.registerPrepareResponse(subscriptionResponder);
        addBehaviour(subscriptionResponder);    // pass handle to proposal behaviour
        
        
        addBehaviour(new TickerBehaviour(this, 5000) {
            @Override
            protected void onTick() {
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.setContent(timestamp() + " adjust x secs (" + subscriptionResponder.getSubscriptions().size() + ")");
                //TODO: content is junction-specific?
                subscriptionResponder.notifyJunctions(inform);
            }
        });
        
        MessageTemplate proposalRespTemplate = MessageTemplate.and(
        MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
        MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE));
        
        proposalResponder = new SectorProposalResp(this, proposalRespTemplate);
        addBehaviour(proposalResponder);

}

String timestamp() {
        StringBuilder sb = new StringBuilder();
        Formatter format = new Formatter(sb, Locale.ENGLISH);
        format.format("%tT ", Calendar.getInstance());
        return sb.toString();
    }

}
