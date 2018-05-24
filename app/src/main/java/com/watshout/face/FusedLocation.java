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
    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference()
            .child("devices")
            .child(CurrentID.getCurrent());

    FusedLocation(Context context, GoogleMap googleMap){

        ArrayList<Marker> markerList = new ArrayList<>();

        this.context = context;
        this.mapPlotter = new MapPlotter(markerList, googleMap);
    }

    public LocationCallback buildLocationCallback() {

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                for (Location location : locationResult.getLocations()){

                    GPSconnected = true;
                    gpsStatus.setText("GPS CONNECTED");
                    gpsStatus.setTextColor(Color.GREEN);

                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    double speed = location.getSpeed();
                    double bearing = location.getBearing();
                    long time = location.getTime();

                    mapPlotter.addMarker(lat, lon);
                    ref.push().setValue(new LocationObject(context, lat, lon, speed, bearing, time));

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
