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

    public String addActivityURL() {
        return getBaseURL() + "/api/addactivity/";
    }
}
