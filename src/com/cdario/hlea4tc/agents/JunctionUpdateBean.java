package com.cdario.hlea4tc.agents;

public class JunctionUpdateBean {

    private int junctionID;
    private double value;
    private String timestamp;

    public JunctionUpdateBean(int junctionID, double value, String timestamp) {
        this.junctionID = junctionID;
        this.value = value;
        this.timestamp = timestamp;
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
