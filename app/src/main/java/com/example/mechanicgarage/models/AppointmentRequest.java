package com.example.mechanicgarage.models;

import com.example.mechanicgarage.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class AppointmentRequest {
    private List<String> requestType = new ArrayList<>();
    private String extraNote;
    private String uid;
    private boolean done = false;
    private String worker = null;
    private int time;

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public AppointmentRequest() {
    }

    public AppointmentRequest(List<String> requestType, String extraNote, String uid, int time) {
        this.requestType = requestType;
        this.extraNote = extraNote;
        this.uid = uid;
        this.done = false;
        this.time = time;
    }

    public List<String> getRequestType() {
        return requestType;
    }

    public void setRequestType(List<String> requestType) {
        this.requestType = requestType;
    }

    public String getExtraNote() {
        return StringUtils.getValueOrEmpty(extraNote);
    }

    public void setExtraNote(String extraNote) {
        this.extraNote = extraNote;
    }

    public String getUid() {
        return StringUtils.getValueOrEmpty(uid);
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public String getWorker() {
        return worker;
    }

    public void setWorker(String worker) {
        this.worker = worker;
    }

    @Override
    public String toString() {
        String requests = "";
        for (String s : requestType) {
            requests.concat(s + ", ");
        }
        return "AppointmentRequest{" +
                "requestType='" + requests + '\'' +
                ", extraNote='" + extraNote + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
