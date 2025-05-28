package co.velandia.hlea4tc.integration.platform;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnvironmentController {

    public static AgentController startJunctionAgent(
        String host,
        String port,
        String name
    ) {
        jade.core.Runtime rt = jade.core.Runtime.instance();
        Profile p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, host);
        p.setParameter(Profile.MAIN_PORT, port);
        ContainerController cc = rt.createAgentContainer(p);

        if (cc != null) {
            try {
                // create and run!
                AgentController jcontroller = cc.createNewAgent(
                    name,
                    "co.velandia.hlea4tc.JunctionAgent",
                    null
                );
                jcontroller.start();
                return jcontroller;
            } catch (StaleProxyException ex) {
                Logger.getLogger(EnvironmentController.class.getName()).log(
                    Level.SEVERE,
                    null,
                    ex
                );
            }
        }
        return null;
    }
}
