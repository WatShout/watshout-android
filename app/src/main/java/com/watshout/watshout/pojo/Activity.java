
package com.watshout.watshout.pojo;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Activity {

    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("map_link")
    @Expose
    private String mapLink;
    @SerializedName("time")
    @Expose
    private Long time;
    @SerializedName("event_name")
    @Expose
    private String eventName;
    @SerializedName("distance")
    @Expose
    private String distance;
    @SerializedName("time_elapsed")
    @Expose
    private String timeElapsed;
    @SerializedName("pace")
    @Expose
    private String pace;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMapLink() {
        return mapLink;
    }

    public void setMapLink(String mapLink) {
        this.mapLink = mapLink;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTimeElapsed() {
        return timeElapsed;
    }

    public void setTimeElapsed(String timeElapsed) {
        this.timeElapsed = timeElapsed;
    }

    public String getPace() { return pace; }

    public void setPace(String pace) { this.pace = pace; }

}
