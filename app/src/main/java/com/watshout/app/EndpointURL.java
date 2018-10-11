package com.watshout.app;

public class EndpointURL {

    private static final EndpointURL ourInstance = new EndpointURL();

    public static EndpointURL getInstance() {
        return ourInstance;
    }

    private EndpointURL() {
    }

    public String getBaseURL() {
        return "https://watshout-cloud.appspot.com";
    }

    public String getHistoryURL(String uid) {
        return getBaseURL() + "/api/history/" + uid + "/";
    }

    public String getFriendURL(String uid) {
        return getBaseURL() + "/api/friends/" + uid + "/";
    }

    public String getFriendRequestURL(String uid) {
        return getBaseURL() + "/api/friendrequests/" + uid + "/";
    }

    public String getNewsFeedURL(String uid) {
        return getBaseURL() + "/api/newsfeed/" + uid + "/";
    }

    public String getStravaURL(String uid, String date){
        return getBaseURL() + "/api/strava/upload/" + uid + "/" + date + "/";
    }

    public String getFriendRequestNotifyURL(){
        return getBaseURL() + "/api/sendfriendnotification/";
    }

    public String addActivityURL() {
        return getBaseURL() + "/api/addactivity/";
    }

    public String getCreateMapURL() {
        return "https://maps.googleapis.com/maps/api/staticmap?" +
                "&style=feature:poi|element:labels.text|visibility:off&style=feature:poi.business|visibility:off&style=feature:road|element:labels.icon|visibility:off&style=feature:transit|visibility:off" +
                "maptype=roadmap&key=AIzaSyDKp7_O42MIn7fMuI2Fbzxo9hslYIWHCIM&sensor=true&path=color:0xff0000ff|enc:";

    }
}
