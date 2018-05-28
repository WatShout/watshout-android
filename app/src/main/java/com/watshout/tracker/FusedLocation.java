package com.watshout.tracker;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

public class FusedLocation {

    private Context context;
    private MapPlotter mapPlotter;
    private String uid;


    FusedLocation(Context context, MapPlotter mapPlotter, String uid){

        this.context = context;
        this.mapPlotter = mapPlotter;
        this.uid = uid;
    }

    public LocationCallback buildLocationCallback() {

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                MainActivity.GPSconnected = true;

                Location location = locationResult.getLocations().get(0);

                double lat = location.getLatitude();
                double lon = location.getLongitude();
                double speed = location.getSpeed();
                double bearing = location.getBearing();
                long time = location.getTime();

                float accuracy = location.getAccuracy();

                Log.wtf("GPSACCURACY", accuracy + "");

                // Adds the point to the map
                mapPlotter.addMarker(lat, lon);

                Log.wtf("GPS", "Lat: " + lat + "\nLong" + lon + "\nTracking: " + MainActivity.currentlyTrackingLocation);

                if (MainActivity.currentlyTrackingLocation){
                    new LocationObject(context, uid, lat, lon, speed, bearing, time).uploadToFirebase();
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
                .setInterval(5000)
                .setFastestInterval(3000)
                .setSmallestDisplacement(5);

        return locationRequest;

    }
}
