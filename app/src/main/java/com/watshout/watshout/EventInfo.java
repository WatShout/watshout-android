package com.watshout.watshout;

public class EventInfo {

    public String type;
    public long time;
    public String distance;
    public String pace;

    EventInfo(String type, long time, String distance, String pace){

        this.type = type;
        this.time = time;
        this.distance = distance;
        this.pace = pace;

    }
}
