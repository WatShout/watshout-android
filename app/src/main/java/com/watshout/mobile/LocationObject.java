package com.watshout.mobile;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

class LocationObject {

    public String uid;
    public double lat;
    public double lon;
    public double speed;
    public double bearing;
    public double battery;
    public double altitude;
    public long time;

    private DatabaseReference ref;

    LocationObject() {

    }

    LocationObject(Context context, String uid, double lat, double lon, double bearing, double speed, double altitude, long time){

        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.bearing = bearing;
        this.battery = getBatteryPercentage(context);
        this.altitude = altitude;
        this.time = time;
        this.ref = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("users")
                .child(uid)
                .child("device")
                .child("current");
    }

    public void uploadToFirebase(){

        ref.push().setValue(this);

    }

    private static double getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (double) (batteryPct * 100);
    }

}
