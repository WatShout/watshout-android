package com.watshout.mobile;

public class EventInfo {

    public String type;
    public long time;
    public String distance;
    public String pace;
    public String map_link;
    public int time_elapsed;

    EventInfo(String type, long time, String distance, String pace, String mapUrl,
              int timeElapsed){

        this.type = type;
        this.time = time;
        this.distance = distance;
        this.pace = pace;
        this.map_link = mapUrl;
        this.time_elapsed = timeElapsed;

    }
}
