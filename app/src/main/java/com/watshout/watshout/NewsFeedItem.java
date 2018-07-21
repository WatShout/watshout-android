package com.watshout.tracker;

public class NewsFeedItem {

    private String name;
    private String imageURL;
    private String time;

    NewsFeedItem(String name, String imageURL, String time) {
        this.name = name;
        this.imageURL = imageURL;
        this.time = time;
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
}
