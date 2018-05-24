package com.watshout.face;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

class LocationObject {

    public Context context;
    public double lat;
    public double lon;
    public double speed;
    public double bearing;
    public double battery;
    public long time;

    LocationObject(Context context, double lat, double lon, double bearing, double speed, long time){

        this.lat = lat;
        this.lon = lon;
        this.speed = speed;
        this.bearing = bearing;
        this.battery = getBatteryPercentage(context);
        this.time = time;
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
