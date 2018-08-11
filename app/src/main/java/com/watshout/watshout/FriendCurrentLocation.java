package com.watshout.watshout;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class FriendCurrentLocation {

    private String initials;
    private LatLng coords;

    FriendCurrentLocation(String initials, LatLng coords) {
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
