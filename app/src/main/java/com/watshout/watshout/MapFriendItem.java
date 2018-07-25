package com.watshout.watshout;

import android.util.Log;

public class MapFriendItem {

    private String initials;

    MapFriendItem(String initials) {
        Log.d("FRIENDS", "Initializing Map Friend Item");
        this.initials = initials;
    }

    public String getInitials() {
        return initials;
    }
}
