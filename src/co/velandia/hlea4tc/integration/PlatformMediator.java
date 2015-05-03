package co.velandia.hlea4tc.integration;

import co.velandia.hlea4tc.Util;
import co.velandia.hlea4tc.agents.JunctionAgent;
import co.velandia.hlea4tc.agents.JunctionUpdateBean;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.ControllerException;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/***
 * Manage globals and container instances here
 * Entry point for the system, mediates between JADE and PARAMICS
 */
public class PlatformMediator {
    
    private static ContainerController container = null;
    private static ContainerController mainContainer = null;
    
    private static JunctionUpdateBean junctionBean;
    
    
    private static final int defaultPort = 1200;
    private static final String junctionPrefix = "J-";
    private static final String sectorPrefix = "S-";
    
    static ArrayList<AgentController> jControllers = new ArrayList<AgentController>();
    static ArrayList<AgentController> sControllers = new ArrayList<AgentController>();
    //private static AgentController rma = null;
    static ArrayList<JunctionAgent> junctions = new ArrayList<JunctionAgent>();

    static ControllerNativeInterface controller;
    // see starting agent from external pp 128 (113)
    
    private static PlatformMediator appInstance = null;

    public static PlatformMediator getInstance() {
        if (appInstance == null){
            appInstance = new PlatformMediator();
        }
        return appInstance;
    }
    
    /**
     * Starts Jade Platform with junctions
     * @param junctions space separated junction identifiers
     * @return success or failure
     */
    public static boolean startJadePlatform(String junctions)
    {
        /*   STARTING JADE FROM AN EXTERNAL JAVA APPLICATION */
        try{
            jade.core.Runtime rt = jade.core.Runtime.instance();
            rt.setCloseVM(true); // close when no containers
            Profile prof = new ProfileImpl(null, defaultPort, null);
            mainContainer = rt.createMainContainer(prof);   // DF is maintained in this container
            ProfileImpl pContainer = new ProfileImpl(null, defaultPort, null);
            pContainer.setParameter(Profile.CONTAINER_NAME, "Traffic-Agents-Container"); 
            container = rt.createAgentContainer(pContainer);
            AgentController rma = container.createNewAgent("rma","jade.tools.rma.rma", new Object[0]);
            rma.start();
           
            String[] junxs = junctions.split("\\s+"); //CREATE AGENTS:
            jControllers.clear();
            sControllers.clear();
            for (String junx : junxs) {
                if(!junx.isEmpty() &&  junx.compareTo(" ")!= 0)
                    initJunctionAgent(junx);
            }
            
            
//            
//            initSectorAgent("101");
//            initSectorAgent("202");
//            initSectorAgent("303");
            
        }catch(StaleProxyException ex)
        {
            Logger.getLogger(PlatformMediator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return (container != null);
    }
    
    /**
     * Add junction to default container
     * @param id new junction identifier
     * @return success or failure
     */
    public static boolean initJunctionAgent(String id)
    {
        if (container != null)
        {
            try {
                AgentController jun = container.createNewAgent(junctionPrefix + id, "co.velandia.hlea4tc.agents.JunctionAgent", new Object[0]);
                jun.start();
                jControllers.add(jun);
                return true;
            } catch (StaleProxyException ex) {
                Logger.getLogger(PlatformMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }

        /**
     * Add sector to default container
     * @param id new sector identifier
     * @return success or failure
     */
    public static boolean initSectorAgent(String id)
    {
        if (container != null)
        {
            try {
                AgentController sec = container.createNewAgent(sectorPrefix + id, "co.velandia.hlea4tc.agents.SectorAgent", new Object[0]);
                sec.start();
                sControllers.add(sec);
                return true;
            } catch (StaleProxyException ex) {
                Logger.getLogger(PlatformMediator.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return false;
    }
    
    
   /**
    * 
    * @param values
    * @return 
    */
    public static String updJunctionAgent(String values)
    {
        if (container != null)
        {
            try {
                String[] junxs = values.split("\\s+"); //CREATE AGENTS:
                int junctionID = Integer.parseInt(junxs[0]);
                //int value =  Integer.parseInt(junxs[1]);
                AgentController tAgent = container.getAgent(junctionPrefix+junctionID, true);  // TODO: untested, get by GUID instead of local name
                tAgent.putO2AObject(new JunctionUpdateBean(junctionID),  AgentController.ASYNC); // TODO: check for SYNC version!
                return "101";
            } catch (StaleProxyException ex) {
                Logger.getLogger(PlatformMediator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ControllerException ecx) {
                Logger.getLogger(PlatformMediator.class.getName()).log(Level.SEVERE, null, ecx);
            }
        }
        return "";
    }
    
       /**
    * 
     * @param junctions
     * @param incoming
    * @return 
    */
    public static String OptAPOinitialise(String junctions, String incoming)
    {
        if (container != null)
        {
            try {
                String[] junxs = junctions.split("\\s+"); //CREATE AGENTS:
                String[] counts = incoming.split("\\s+"); //CREATE AGENTS:
                int junctionID = Integer.parseInt(junxs[0]);    //[o,n,s,e,w]
                AgentController tAgent = container.getAgent(junctionPrefix+junctionID, true);  // TODO: untested, get by GUID instead of local name
                JunctionUpdateBean junctBean = new JunctionUpdateBean(junctionID);
                junctBean.goodList = new ArrayList(5);
                junctBean.agentView = new ArrayList(4);
                junctBean.incomingCounts = new ArrayList(4);
                junctBean.goodList.add(junctionID);

                int prio = 0;

                for (int i=1;i < 5; i++) {
                    junctBean.goodList.add(Integer.parseInt(junxs[i]));
                    junctBean.agentView.add(Integer.parseInt(junxs[i]));
                    int count = Integer.parseInt(counts[i]);
                    junctBean.incomingCounts.add(count);
                    prio +=count;
                }
                
                junctBean.priority = prio;
                junctBean.coordination = (int)(Math.random() * 1); 
                junctBean.activeMediation = true;
                
                tAgent.putO2AObject(junctBean, AgentController.ASYNC); // TODO: check for SYNC version!
                return "101";
            } catch (StaleProxyException ex) {
                Logger.getLogger(PlatformMediator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ControllerException ecx) {
                Logger.getLogger(PlatformMediator.class.getName()).log(Level.SEVERE, null, ecx);
            }
        }
        return "";
    }
    
    
    public static String OptAPOcheckAgentView(String junctionID) throws ControllerException
    {
        if (container != null)
        {
            
            // calculate current cost F within subgraph goodList
            //[n,s,e,w]
            AgentController tAgent = container.getAgent(junctionPrefix+junctionID, true); 
            
            JunctionAgent junction = (JunctionAgent)tAgent;        // TODO: invalid cast; alt 
            
            junctionBean.coordination = 
            
            junction.myState.junctionID;
            
            
        }     
        return null;
       
    }
    
}
