package com.watshout.watshout;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class MapFriendItem {

    private String initials;
    private LatLng coords;

    MapFriendItem(String initials, LatLng coords) {
        Log.d("FRIENDS", "Initializing Map Friend Item");
        this.initials = initials;
        this.coords = coords;
    }

    public String getInitials() {
        return initials;
    }

    public void setCoords(LatLng coords){
        this.coords = coords;
    }

    public LatLng getCoords() {
        return coords;
    }
}
