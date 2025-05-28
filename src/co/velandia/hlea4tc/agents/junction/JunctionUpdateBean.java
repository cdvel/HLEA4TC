package co.velandia.hlea4tc.agents.junction;

import java.util.ArrayList;

public class JunctionUpdateBean {

    public int junctionID;

    public int coordination; //0: E-W; 1: N-S
    public int priority;
    public boolean activeMediation;
    public ArrayList incomingCounts;
    public ArrayList goodList;
    public ArrayList agentView;

    private double value;
    private String timestamp;

    public JunctionUpdateBean(int junctionID) {
        this.junctionID = junctionID;
    }

    public int getJunctionID() {
        return junctionID;
    }

    public void setJunctionID(int junctionID) {
        this.junctionID = junctionID;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
