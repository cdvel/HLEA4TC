package co.velandia.hlea4tc.communication.protocols.negotiation;

import co.velandia.hlea4tc.agents.sector.SectorAgent;
import co.velandia.hlea4tc.communication.protocols.subscription.SectorSubscriptionResp;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.proto.ProposeInitiator;
import java.util.ArrayList;
import java.util.Vector;

public class SectorProposalInit extends ProposeInitiator {

    ArrayList<AID> knownSectors;

    public SectorProposalInit(SectorAgent a, ACLMessage msg) {
        super(a, msg);
        knownSectors = a.getKnownSectors();
    }

    /*
     * Broadcast your interest in a junction to all intersections
     */
    @Override
    protected Vector prepareInitiations(ACLMessage propose) {
        // Retrieve the incoming subscription from the DataStore
        String incomingSubscriptionKey =
            (String) ((SectorSubscriptionResp) parent).SUBSCRIPTION_KEY;
        ACLMessage incomingSubscription = (ACLMessage) getDataStore()
            .get(incomingSubscriptionKey);
        // Prepare the request to forward to the responder
        System.out.println(
            "[S] " +
            myAgent.getLocalName() +
            "\t = PROPOSE subscribe junction " +
            incomingSubscription.getSender().getLocalName() +
            " to other sectors (x" +
            knownSectors.size() +
            ")"
        );
        Vector v = new Vector(1);
        for (int s = 0; s < knownSectors.size(); s++) {
            ACLMessage outgoingPropose = new ACLMessage(ACLMessage.PROPOSE);
            outgoingPropose.setProtocol(
                FIPANames.InteractionProtocol.FIPA_PROPOSE
            );
            outgoingPropose.addReceiver(knownSectors.get(s));
            outgoingPropose.setContent(incomingSubscription.getContent());
            outgoingPropose.setReplyByDate(
                incomingSubscription.getReplyByDate()
            );
            v.addElement(outgoingPropose);
        }
        return v;
    }

    @Override
    protected void handleAcceptProposal(ACLMessage accept_proposal) {
        //super.handleAcceptProposal(accept_proposal); //To change body of generated methods, choose Tools | Templates.
        // TODO: send agree message to subscriber
        //System.out.println("--> Agent " + myAgent.getLocalName() + ": received accept proposal from"+accept_proposal.getSender().getLocalName());
        System.out.println(
            "[S] " +
            myAgent.getLocalName() +
            "\t @ ACCEPTED " +
            accept_proposal.getContent()
        );
        /*
         * transform the accept-proposal into agree-subscribe
         *
         */
        storeNotification(ACLMessage.ACCEPT_PROPOSAL, accept_proposal);
    }

    @Override
    protected void handleRejectProposal(ACLMessage reject_proposal) {
        //super.handleRejectProposal(reject_proposal); //To change body of generated methods, choose Tools | Templates.
        // TODO: send refuse to subscriber
        System.out.println(
            "[S] " +
            myAgent.getLocalName() +
            "\t @ REJECTED " +
            reject_proposal.getContent()
        );
        storeNotification(ACLMessage.REJECT_PROPOSAL, reject_proposal);
    }

    @Override
    protected void handleNotUnderstood(ACLMessage notUnderstood) {
        storeNotification(ACLMessage.NOT_UNDERSTOOD, notUnderstood);
    }

    //        @Override
    //    protected void handleAllResponses(Vector responses) {
    //           System.out.println("--> Agent " + myAgent.getLocalName() + ": received RESPONSES from"+responses.size());
    //        }

    //    @Override
    //    protected void handleAllResponses(Vector responses) {
    //        if (responses.size() == 0) {
    //            storeNotification(ACLMessage.FAILURE, null);
    //        }
    //    }

    private void storeNotification(int performative, ACLMessage original) {
        //        if (performative == ACLMessage.ACCEPT_PROPOSAL) {
        //            System.out.println("[S] " + myAgent.getLocalName() + "\t @ ACCEPTED "+original.getContent());
        //        } else {
        //            System.out.println("[S] " + myAgent.getLocalName() + "\t @ REJECTED "+original.getContent());
        //        }

        // Retrieve the incoming request from the DataStore
        String incomingSubscriptionkey =
            (String) ((SectorSubscriptionResp) parent).SUBSCRIPTION_KEY;
        ACLMessage incomingSubscription = (ACLMessage) getDataStore()
            .get(incomingSubscriptionkey);
        // Prepare the notification to the request originator and store it in the DataStore
        ACLMessage notification = incomingSubscription.createReply();
        notification.setPerformative(performative);
        if (original != null) notification.setContent(original.getContent());
        String notificationkey =
            (String) ((SectorSubscriptionResp) parent).RESPONSE_KEY;
        getDataStore().put(notificationkey, notification);
    }
}
