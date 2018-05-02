package com.watshout.face;

import com.google.firebase.database.IgnoreExtraProperties;

@IgnoreExtraProperties
public class LocationData {

    double latitude;
    double longitude;

    public LocationData(){}

    public LocationData(double latitude, double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
    }

}
