package co.velandia.hlea4tc.integration.simulation;

import co.velandia.hlea4tc.agents.junction.JunctionAgent;
import co.velandia.hlea4tc.integration.platform.EnvironmentController;

public class ControllerNativeInterface {
    static {
        System.load(
            "C:\\Users\\cesar\\Dropbox\\CODE\\HLEA4TCSim\\dist\\ControllerNativeInterface.dll"
        );
    }

    /*
        public static String callControllerWrapper(String id, int value) {
            return new ControllerNativeInterface().callUpdate(id, value);
        }

        private native String callUpdate(String id, int value);
    */

    private native void createJunction(String id, int value, int value2);

    public void startJadeJunctionAgent(String agentName) {
        EnvironmentController.startJunctionAgent("host", "port", agentName);
    }

    private native JunctionAgent getJunctionAgent();

    public JunctionAgent getNewJunctionFromSimulation() {
        //ControllerNativeInterface controller = new ControllerNativeInterface();
        return getJunctionAgent();
        // do something, keep in array, add to platform etc

    }
}
