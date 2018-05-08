package com.watshout.face;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static com.watshout.face.MainActivity.GPSconnected;
import static com.watshout.face.MainActivity.gpsStatus;

class MyLocationListener implements LocationListener {

    // Takes location data and (eventually) gets it into JSON format
    private String parseGPSData(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        long time = location.getTime();
        double speed = location.getSpeed();
        double bearing = (double) location.getBearing();

        ArrayList<String> keys = new ArrayList<>();
        keys.add("lat");
        keys.add("long");
        keys.add("time");
        keys.add("speed");
        keys.add("bearing");

        ArrayList<Double> values = new ArrayList<>();
        values.add(lat);
        values.add(lon);
        values.add((double) time);
        values.add(speed);
        values.add(bearing);

        return makeJSON(keys, values);

    }

    // Creates beautiful JSON without having to rip your hair out
    private String makeJSON(ArrayList keys, ArrayList values){

        String returnMe = "{";

        if (keys.size() == values.size()){

            for (int i = 0; i < keys.size(); i++) {

                String key = (String) keys.get(i);
                double value = (double) values.get(i);

                returnMe += "\"" + key + "\": " + value;

                if (i != keys.size() - 1) {
                    returnMe += ", ";

                }
            }
        }

        returnMe += "}";

        return returnMe;
    }

    public void onLocationChanged(Location location) {

        // Logcat message
        String message = String.format(
                "New Location \n Longitude: %1$s \n Latitude: %2$s",
                location.getLongitude(), location.getLatitude()
        );

        // Parse data, then POST using PostData class
        String data = parseGPSData(location);
        PostData post = new PostData();

        String id = CurrentID.getCurrent();

        LocationData test = new LocationData(location.getLatitude(), location.getLongitude());

        DatabaseReference thisDB = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(CurrentID.getCurrent());

        //thisDB.push().setValue(test);

        post.execute(data, id);

        Log.v("GPSDATA", message);

        GPSconnected = true;

        gpsStatus.setText("GPS CONNECTED");
        gpsStatus.setTextColor(Color.GREEN);

    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                Log.v("GPSDATA", "OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.v("GPSDATA", "TEMPORARILY_UNAVAILABLE");
                break;
            case LocationProvider.AVAILABLE:
                Log.v("GPSDATA", "AVAILABLE");
                break;
        }
    }

    public void onProviderDisabled(String provider) {
        Log.v("GPSDATA", "Disabled");
    }

    public void onProviderEnabled(String provider) {
        Log.v("GPSDATA", "Enabled");
    }

}