package com.watshout.mobile;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.preference.PreferenceManager;
import android.widget.TextView;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.alternativevision.gpx.beans.Waypoint;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import static android.content.Context.SENSOR_SERVICE;

public class FusedLocation implements SensorEventListener {

    private Context context;
    private static MapPlotter mapPlotter;
    private String uid;
    private ArrayList<Waypoint> trackPoints;
    public static double latitude = 0;
    public static double longitude = 0;
    ArrayList<Double> preLat;
    ArrayList<Double> preLon;
    double prevLat;
    double prevLon;
    double distance;

    double recentHeight;

    public int name;

    boolean hasBarometer;

    boolean out;
    double info [][] = new double [3][3];

    ArrayList<String> bearingArr = new ArrayList<String>();
    ArrayList<String> speedArr = new ArrayList<String>();


    TextView speedTextDialog;
    TextView stepsDialog;

    static TextView distanceDialog;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    List<LatLng> latLngList;
    int time;

    SensorManager mSensorManager;
    List<Sensor> sensors;
    Sensor sensor;

    private final static int ONGOING_NOTIFICATION_ID = 65050;

    GoogleMap googleMap;

    private static final double MS_TO_MM = 26.8224;

    private final static String TAG = "FusedLocation";

    FusedLocation(Context context, MapPlotter mapPlotter, String uid, TextView speedTextDialog,
                  TextView stepsDialog, TextView distanceDialog, ArrayList preLat, ArrayList preLon,
                  GoogleMap googleMap)
            throws TransformerException, ParserConfigurationException {

        this.context = context;
        this.mapPlotter = mapPlotter;
        this.uid = uid;
        this.trackPoints = new ArrayList<>();
        this.speedTextDialog = speedTextDialog;
        this.stepsDialog = stepsDialog;
        this.distanceDialog = distanceDialog;
        this.settings = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = settings.edit();
        this.latLngList = new ArrayList<>();
        this.preLat = preLat;
        this.preLon = preLon;
        distance = 0;
        out = false;
        this.googleMap = googleMap;

        //prevLat = Double.valueOf(settings.getString("last_latitude", "37.4419"));
        //prevLon = Double.valueOf(settings.getString("last_longitude", "-122.1430"));

        prevLat = 0;
        prevLon = 0;
        Random random = new Random();
        this.name = random.nextInt(100000);


    }

    public void resetLatLng() {
        this.latLngList = new ArrayList<>();
    }

    public List getLatLng() {
        return latLngList;
    }

    private void updateSharedPreferenceValues(double lat, double lon){

        editor.putString("last_latitude", lat + "");
        editor.putString("last_longitude", lon + "");
        editor.commit();

    }

    // master

    private String parsePace(double minuteMilePace) {

        double minutes = Math.floor(minuteMilePace);

        double seconds = Math.floor((minuteMilePace - minutes) * 60);

        return (int) minutes + ":" + (int) seconds;

    }

    public LocationCallback buildLocationCallback() {

        mSensorManager = (SensorManager) context.getSystemService(SENSOR_SERVICE);
        sensors = mSensorManager.getSensorList(Sensor.TYPE_PRESSURE);


        if (sensors.size() > 0)
        {
            hasBarometer = true;
            sensor = sensors.get(0);
            mSensorManager.registerListener(this, sensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        else
            hasBarometer = false;

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                MapFragment.GPSconnected = true;

                Location location = locationResult.getLocations().get(0);

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                updateSharedPreferenceValues(latitude, longitude);

                double lat = location.getLatitude();
                double lon = location.getLongitude();
                double speed = location.getSpeed();
                double bearing = location.getBearing();
                double altitude;
                if(hasBarometer)
                 altitude = recentHeight;
                else
                    altitude = location.getAltitude();
                long time = location.getTime();
                float accuracy = location.getAccuracy();


                // Center map on current location
                LatLng now = new LatLng(lat, lon);
                //googleMap.moveCamera(CameraUpdateFactory.newLatLng(now));

                DecimalFormat pace = new DecimalFormat("##");

                //Checks if speed is positive and if 0, then prints 0
                if (speed > 0) {
                    // Speed is in m/s
                    double minuteMilePace = MS_TO_MM / speed;

                    int minutes = (int) Math.floor(minuteMilePace);
                    int seconds = (int) Math.floor((minuteMilePace - minutes) * 60);

                    String minString = pace.format(minutes);
                    String secString = pace.format(seconds);

                    speedTextDialog.setText(minString + ":" + secString);
                } else {
                    speedTextDialog.setText("00:00");
                }

                if (MapFragment.currentlyTrackingLocation){
                    bearingArr.add(bearing + "");
                    speedArr.add(speed + "");

                    //int size = bearingArr.size();
                    //int speedSize = speedArr.size();

                   double addDistance = 0;

                   /* if(out == false && preLat!= null) {
                        for(int x = 0; x < preLat.size(); x ++)
                            mapPlotter.addMarker(preLat.get(x), preLon.get(x));
                        out = true;
                        for(int a = 1; a < preLat.size(); a ++){
                            //This was commented out before
                            addDistance = calculationByDistance(preLat.get(a-1)*Math.PI/180,preLon.get(a-1)*Math.PI/180,
                                    preLat.get(a)*Math.PI/180, preLon.get(a)*Math.PI/180);
                        distance = distance + addDistance;
                            }

                    }*/


                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    int usedName = preferences.getInt("fusedLocationName", -1);

                    if (name == usedName) {
                        bearingArr.add(bearing + "");
                        speedArr.add(speed + "");

                        /*This is if the app is reopened so we need to check if it is the first time.
                    The second part checks if the previousLatitude is null meaning we don't have
                    data points saved*/
                        if (out == false && preLat!= null) {
                            for(int x = 0; x < preLat.size(); x ++)
                                mapPlotter.addMarker(preLat.get(x), preLon.get(x));
                            out = true;
                        }

                        mapPlotter.addMarker(lat, lon);

                        new LocationObject(context, uid, lat, lon, speed, bearing, altitude, time).uploadToFirebase();
                        latLngList.add(new LatLng(lat, lon));

                    /*This mini if statement checks if only one coordinate is in the system; then
                        distance cannot be calculated, otherwise calculate the distance*/
                    if (prevLat < 2) {
                        if (distance > 0) {}
                        else {distance = 0;
                        //System.out.println("Distance became 0:2");
                        }
                    }

                    else {
                        distance +=  calculationByDistance(prevLat*Math.PI/180,prevLon*Math.PI/180,
                                lat*Math.PI/180, lon*Math.PI/180);
                        //System.out.println("Distance added:1");

                    }

                    double tempDistance =  distance;
                    double miles = tempDistance * 0.000621371;
                    //System.out.println("COORDS:" + lat + ", " + lon);
                        //System.out.println("Distance: " + miles);

                    DecimalFormat decimalFormat = new DecimalFormat("##.###");
                    distanceDialog.setText(decimalFormat.format(miles));

                    prevLat = lat;
                    prevLon = lon;
                    }
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
                .setFastestInterval(4000)
                .setSmallestDisplacement(0);

        return locationRequest;

    }

    public void setCurrentRunningTime(int time){
        this.time = time;
    }


    public double calculationByDistance(double initialLat, double initialLong, double finalLat, double finalLong){
        /*PRE: All the input values are in radians!*/

        double latDiff = finalLat - initialLat;
        double longDiff = finalLong - initialLong;
        double earthRadius = 6371; //In Km if you want the distance in km

        double distanceTemp = 1000*2*earthRadius*Math.asin(Math.sqrt(Math.pow(Math.sin(latDiff/2.0),2)+
                Math.cos(initialLat)*Math.cos(finalLat)*Math.pow(Math.sin(longDiff/2),2)));

        return distanceTemp;

    }

    public double[] getLatestCoords() {

        double[] coords = new double[2];

        coords[0] = latitude;
        coords[1] = longitude;

        return coords;
    }

    public boolean is_angle_between(int target, int angle1, int angle2)
    {
        // make the angle from angle1 to angle2 to be <= 180 degrees
        int rAngle = ((angle2 - angle1) % 360 + 360) % 360;
        if (rAngle >= 180) {
            int temp = angle1;
            angle1 = angle2;
            angle2 = temp;
        }
        // check if it passes through zero
        if (angle1 <= angle2)
            return target >= angle1 && target <= angle2;
        else
            return target >= angle1 || target <= angle2;
    }

    private String metersPerSecondToMinutesPerKilometer(double speed, boolean metric) {

        if (speed != 0) {
            double pace = (1 / speed) / 60 * 1000;


            int minutes = (int) Math.floor(pace);


            int seconds = (int) Math.floor((pace - minutes) * 60);

            return minutes + ":" + seconds;
        } else {
            return "0:00";
        }

    }
    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM
    public static double distanceBetweenTwoCoordinates(double startLat, double startLong,
                                  double endLat, double endLong) {

        double dLat  = Math.toRadians((endLat - startLat));
        double dLong = Math.toRadians((endLong - startLong));

        startLat = Math.toRadians(startLat);
        endLat   = Math.toRadians(endLat);

        double a = haversin(dLat) + Math.cos(startLat) * Math.cos(endLat) * haversin(dLong);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c; // <-- d
    }

    public static double haversin(double val) {
        return Math.pow(Math.sin(val / 2), 2);
    }

    /* if(size >= 3){
                        int a = (int)Double.parseDouble(bearingArr.get(size-1));
                        int b = (int)Double.parseDouble(bearingArr.get(size-2));
                        int c = (int)Double.parseDouble(bearingArr.get(size-3));
                        int aDiff1 = (a - 90)%360;
                        int aDiff2 = (a + 90)%360;
                        int bDiff1 = (b - 90)%360;
                        int bDiff2 = (b + 90)%360;
                        if(is_angle_between(b, aDiff1, aDiff2) == false){
                            if(is_angle_between(c, bDiff1, bDiff2) == false){Log.d("Bearing is", bearing + "");}
                                else {Log.d("Tagis", "Method ended");
                                return;
                            }
                        }
                    }*/

    /* if(speedSize >= 2) {
                        //d = vt + 0.5(vf-v)t
                        double vFinal = Double.parseDouble(speedArr.get(speedSize - 1));
                        double vInitial = Double.parseDouble(speedArr.get(speedSize - 2));
                        //long tFinal = Long.parseLong(timeArr.get(speedSize - 1));
                        //long tInitial = Long.parseLong(timeArr.get(speedSize - 2));
                        double aveSpeed = 0.5*(vFinal - vInitial);
                        if(aveSpeed > 10)
                            return;
                    }*/

    @Override
    public void onSensorChanged(SensorEvent event) {
        // when pressure value is changed, this method will be called.
        float pressure_value = 0.0f;
        float height = 0.0f;

        // if you use this listener as listener of only one sensor (ex, Pressure), then you don't need to check sensor type.
        if (Sensor.TYPE_PRESSURE == event.sensor.getType()) {
            pressure_value = event.values[0];
            //System.out.println("PRESSURE " + pressure_value);
            height = SensorManager.getAltitude(SensorManager.PRESSURE_STANDARD_ATMOSPHERE, pressure_value);
            recentHeight = height;
            //heightDialog.setText(height + " m");
           // System.out.println("HEIGHT " + height);
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Do something here if sensor accuracy changes.
        // You must implement this callback in your code.

    }

}
