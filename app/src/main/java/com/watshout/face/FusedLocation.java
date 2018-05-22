package com.watshout.face;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.os.BatteryManager;
import android.util.Log;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static com.watshout.face.MainActivity.GPSconnected;
import static com.watshout.face.MainActivity.gpsStatus;

public class FusedLocation {

    private Context context;

    FusedLocation(Context context){
        this.context = context;
    }

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
        keys.add("battery");

        ArrayList<Double> values = new ArrayList<>();
        values.add(lat);
        values.add(lon);
        values.add((double) time);
        values.add(speed);
        values.add(bearing);
        values.add(getBatteryPercentage(context));

        return makeJSON(keys, values);

    }

    private static double getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (double) (batteryPct * 100);
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

    public LocationCallback buildLocationCallback() {

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //super.onLocationResult(locationResult);
                for (Location location : locationResult.getLocations()){

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

                    GPSconnected = true;

                    gpsStatus.setText("GPS CONNECTED");
                    gpsStatus.setTextColor(Color.GREEN);

                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                //super.onLocationAvailability(locationAvailability);
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
