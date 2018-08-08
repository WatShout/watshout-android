package com.watshout.watshout;

public class NewsFeedItem {

    private String name;
    private String imageURL;
    private String time;
    private String activityName;

    NewsFeedItem(String name, String imageURL, String time, String activityName) {
        this.name = name;
        this.imageURL = imageURL;
        this.time = time;
        this.activityName = activityName;
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
}
