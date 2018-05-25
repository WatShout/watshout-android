package com.watshout.face;

import android.content.Context;
import android.graphics.Color;
import android.location.Location;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.watshout.face.MainActivity.GPSconnected;
import static com.watshout.face.MainActivity.gpsStatus;

public class FusedLocation {

    private Context context;
    private MapPlotter mapPlotter;


    FusedLocation(Context context, MapPlotter mapPlotter){

        this.context = context;
        this.mapPlotter = mapPlotter;
    }

    public LocationCallback buildLocationCallback() {

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                GPSconnected = true;

                for (Location location : locationResult.getLocations()){

                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    double speed = location.getSpeed();
                    double bearing = location.getBearing();
                    long time = location.getTime();

                    // Adds the point to the map
                    mapPlotter.addMarker(lat, lon);

                    // Uploads to Firebase (eventually should add a condition check here)
                    new LocationObject(context, lat, lon, speed, bearing, time).uploadToFirebase();

                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {

            }
        };

        return locationCallback;

    }

    public LocationRequest buildLocationRequest() {

        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(4000)
                .setFastestInterval(2000)
                .setSmallestDisplacement(3);

        return locationRequest;

    }
}
