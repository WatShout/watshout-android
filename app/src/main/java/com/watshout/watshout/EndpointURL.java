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
        return getBaseURL() + "/maps/calendar/download/" + uid + "/";
    }

    public String getFriendURL(String uid) {
        return getBaseURL() + "/friends/" + uid + "/";
    }

    public String getMapsURL(String uid) {
        return getBaseURL() + "/maps/download/" + uid + "/";
    }

    public String getStravaURL(String uid, String date){
        return getBaseURL() + "/mobile/strava/" + uid + "/" + date + "/";
    }

    public String createMapURL() {
        return getBaseURL() + "/create-map/";
    }
}
