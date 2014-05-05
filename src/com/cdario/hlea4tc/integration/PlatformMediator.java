package com.cdario.hlea4tc.integration;

import com.cdario.hlea4tc.Util;
import com.cdario.hlea4tc.agents.JunctionAgent;
import com.cdario.hlea4tc.agents.JunctionUpdateBean;
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
    
    private static final int defaultPort = 1200;
    private static final String junctionPrefix = "J-";
    
    static ArrayList<AgentController> jControllers = new ArrayList<AgentController>();
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
            for (String junx : junxs) {
                initJunctionAgent(junx);
            }
            
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
                AgentController jun = container.createNewAgent(junctionPrefix + id, "com.cdario.hlea4tc.agents.JunctionAgent", new Object[0]);
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
     * new value for the existing junction
     * @param id  junction identifier
     * @param value 
     * @return 
     */
    public static boolean updJunctionAgent(int id, double value)
    {
        if (container != null)
        {
            try {
                AgentController tAgent = container.getAgent(junctionPrefix+id, true);  // TODO: untested, get by GUID instead of local name
                tAgent.putO2AObject(new JunctionUpdateBean(id, value, Util.timestamp()), AgentController.ASYNC); // TODO: check for SYNC version!
                return true;
            } catch (StaleProxyException ex) {
                Logger.getLogger(PlatformMediator.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ControllerException ecx) {
                Logger.getLogger(PlatformMediator.class.getName()).log(Level.SEVERE, null, ecx);
            }
        }
        return false;
    }

    
    //public static void main(String[] args) {
        
        //  1. start JADE platform, get container details etc
        
        //  2. 
       
        //controller = new ControllerNativeInterface();
        //addJunction(controller.getNewJunctionFromSimulation());
        
    //}
    
    private static void addJunction(JunctionAgent newJunction) {
        junctions.add(newJunction);
    }
    
}
