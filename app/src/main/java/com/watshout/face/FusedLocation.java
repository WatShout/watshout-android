package com.watshout.face;

import android.content.Context;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;

import static com.watshout.face.MainActivity.GPSconnected;
import static com.watshout.face.MainActivity.currentlyTrackingLocation;

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

                Log.wtf("GPS", "Lat: " + lat + "\nLong" + lon);

                if (currentlyTrackingLocation){
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
                .setInterval(5000)
                .setFastestInterval(3000)
                .setSmallestDisplacement(5);

        return locationRequest;

    }
}
