package com.watshout.tracker;

public class CalendarItem {
    private String imageURL;
    private String time;

    CalendarItem(String imageURL, String time) {
        this.imageURL = imageURL;
        this.time = time;
    }

    public String getImageURL() {
        return imageURL;
    }

    public String getTime() {
        return time;
    }
}
