package com.watshout.watshout;

public class EndpointURL {

    private static final EndpointURL ourInstance = new EndpointURL();

    public static EndpointURL getInstance() {
        return ourInstance;
    }

    private EndpointURL() {
    }

    private String getBaseURL() {
        return "https://watshout-test.appspot.com";
    }

    public String getHistoryURL(String uid) {
        return getBaseURL() + "/history/" + uid + "/";
    }

    public String getFriendURL(String uid) {
        return getBaseURL() + "/friends/" + uid + "/";
    }

    public String getFriendRequestURL(String uid) {
        return getBaseURL() + "/friendrequests/" + uid + "/";
    }

    public String getNewsFeedURL(String uid) {
        return getBaseURL() + "/newsfeed/" + uid + "/";
    }

    public String getStravaURL(String uid, String date){
        return getBaseURL() + "/strava/upload/" + uid + "/" + date + "/";
    }

    public String createMapURL() {
        return getBaseURL() + "/create-map/";
    }
}
