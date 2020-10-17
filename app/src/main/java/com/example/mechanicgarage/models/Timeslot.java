package com.example.mechanicgarage.models;

public class Timeslot {
    private boolean taken;
    private int time;
    private String reqId;
    private String uid;

    public Timeslot() {
    }

    public Timeslot(boolean taken, int time, String reqId) {
        this.taken = taken;
        this.time = time;
        this.reqId = reqId;
    }

    public boolean isTaken() {
        return taken;
    }

    public void setTaken(boolean taken) {
        this.taken = taken;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public String getReqId() {
        return reqId;
    }

    public void setReqId(String reqId) {
        this.reqId = reqId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
