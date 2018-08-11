package com.watshout.watshout;

public class EventInfo {

    public String type;
    public long time;
    public String distance;
    public String pace;
    public String map_link;

    EventInfo(String type, long time, String distance, String pace, String mapUrl){

        this.type = type;
        this.time = time;
        this.distance = distance;
        this.pace = pace;
        this.map_link = mapUrl;

    }
}
