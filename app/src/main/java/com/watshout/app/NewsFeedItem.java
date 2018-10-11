package com.watshout.app;

public class NewsFeedItem {

    private String name;
    private String imageURL;
    private String time;
    private String activityName;
    private String distance;
    private String timeElapsed;

    NewsFeedItem(String name, String imageURL, String time, String activityName,
                 String distance, String timeElapsed) {
        this.name = name;
        this.imageURL = imageURL;
        this.time = time;
        this.activityName = activityName;
        this.distance = distance;
        this.timeElapsed = timeElapsed;
    }

    public String getName() {
        return name;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getTime() {
        return time;
    }

    public String getActivityName() {return activityName;}

    public String getDistance() {return distance;}

    public String getTimeElapsed() {return timeElapsed;}
}
