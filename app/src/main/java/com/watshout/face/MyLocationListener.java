package com.watshout.face;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import static com.watshout.face.MainActivity.gpsStatus;

class MyLocationListener implements LocationListener {

    // Takes location data and turns it into JSON-like string
    private String parseGPSData(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        long time = location.getTime();
        double speed = location.getSpeed();
        double bearing = (double) location.getBearing();

        HashMap<String, Double> allParams = new HashMap<>();

        allParams.put("lat", lat);
        allParams.put("long", lon);
        allParams.put("time", (double) time);
        allParams.put("speed", speed);
        allParams.put("bearing", bearing);

        Log.v("JSON", makeJSON(allParams));


        return "{\"lat\": " + lat + ", \"long\": " + lon + ", \"speed\": " + speed + ", \"time\": " + time + "}";
    }

    private String makeJSON(HashMap params){

        String returnMe = "{";

        ArrayList keySet = (ArrayList) params.keySet();

        for (int i = 0; i < params.size();  i++){

            String key = keySet.get(i).toString();
            String value = params.get(key).toString();

            returnMe += "\"" + key + "\": " + value;

            if (i != params.size() - 1){
                returnMe += ", ";
            }

        }

        returnMe += "}";

        return returnMe;
    };

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

        post.execute(data, id);

        Log.v("GPSDATA", message);

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