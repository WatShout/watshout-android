package com.watshout.tracker;

public class NewsFeedItem {

    private String title;
    private String imageURL;

    NewsFeedItem(String title, String imageURL) {
        this.title = title;
        this.imageURL = imageURL;
    }

    public String getTitle() {
        return title;
    }

    public String getImageURL() {
        return imageURL;
    }
}
