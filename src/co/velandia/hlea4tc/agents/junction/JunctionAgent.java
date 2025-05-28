package co.velandia.hlea4tc.agents.junction;

import co.velandia.hlea4tc.communication.protocols.coordination.SectorManager;
import co.velandia.hlea4tc.communication.protocols.subscription.JunctionSubscriptionInit;
import co.velandia.hlea4tc.integration.platform.PlatformMediator;
import co.velandia.hlea4tc.integration.simulation.ControllerNativeInterface;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import java.util.ArrayList;

public class JunctionAgent extends Agent {

    protected String junctionID;
    protected AID mySector;
    protected ArrayList<AID> knownSectors;
    protected ControllerNativeInterface controller;
    public String value;
    public JunctionUpdateBean myState;

    private final PlatformMediator app = PlatformMediator.getInstance();

    /**
     *
     */
    @Override
    protected void setup() {
        knownSectors = new ArrayList<AID>();
        junctionID = getLocalName();
        setEnabledO2ACommunication(true, AP_MAX); // permits interactions from Mediator
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
        SectorManager sectorUpdater = new SectorManager(
            this,
            DFService.createSubscriptionMessage(
                this,
                getDefaultDF(),
                description,
                null
            ),
            knownSectors
        );
        addBehaviour(sectorUpdater);

        /*
         * attempt subscribe every 5 seconds
         */

        //        final JunctionSubscriptionInit initSub = new JunctionSubscriptionInit(this);
        //        addBehaviour(initSub);

        addBehaviour(
            new TickerBehaviour(this, 8000) {
                @Override
                protected void onTick() {
                    myAgent.addBehaviour(
                        new JunctionSubscriptionInit((JunctionAgent) myAgent)
                    );
                }
            }
        );

        // start monitoring junction update beans
        addBehaviour(
            new CyclicBehaviour(this) {
                @Override
                public void action() {
                    // get an object from the O2A mailbox
                    Object myObject = myAgent.getO2AObject();

                    // if we actually got one
                    if (myObject != null) {
                        JunctionUpdateBean bn = (JunctionUpdateBean) myObject; //do something
                        //value = ""+bn.getValue();
                        myState = bn;
                    } else {
                        block();
                    }
                }
            }
        );
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

    public ArrayList<AID> getKnownSectors() {
        return knownSectors;
    }

    public AID getMySector() {
        return mySector;
    }
}
