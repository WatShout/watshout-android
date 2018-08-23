package com.watshout.watshout;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.maps.model.LatLng;

import org.alternativevision.gpx.beans.TrackPoint;
import org.alternativevision.gpx.beans.Waypoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class FusedLocation  {

    private Context context;
    private MapPlotter mapPlotter;
    private String uid;
    private ArrayList<Waypoint> trackPoints;
    private XMLCreator XMLCreator;
    public static double latitude = 0;
    public static double longitude = 0;
    //double speed;
    double prevLat;
    double prevLon;
    double distance;

    double info [][] = new double [3][3];
    //double bearingArr [] = new double [3];
    ArrayList<String> bearingArr = new ArrayList<String>();
    ArrayList<String> speedArr = new ArrayList<String>();
    //ArrayList<String> timeArr = new ArrayList<String> ();

    TextView speedTextDialog;
    TextView stepsDialog;
    TextView distanceDialog;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    List<LatLng> latLngList;
    int time;

    private final static String TAG = "FusedLocation";

    FusedLocation(Context context, MapPlotter mapPlotter, String uid,
                  XMLCreator XMLCreator, TextView speedTextDialog,
                  TextView stepsDialog, TextView distanceDialog)
            throws TransformerException, ParserConfigurationException {

        this.context = context;
        this.mapPlotter = mapPlotter;
        this.uid = uid;
        this.trackPoints = new ArrayList<>();
        this.XMLCreator = XMLCreator;
        this.speedTextDialog = speedTextDialog;
        this.stepsDialog = stepsDialog;
        this.distanceDialog = distanceDialog;
        this.settings = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = settings.edit();

        this.latLngList = new ArrayList<>();
        distance = 0;

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

    public LocationCallback buildLocationCallback() {

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
                Log.d("Speed count",  "" + speed);
                //Toast.makeText(context, speed + "", Toast.LENGTH_SHORT).show();
                double bearing = location.getBearing();
                double altitude = location.getAltitude();
                long time = location.getTime();
                float accuracy = location.getAccuracy();


                double newSpeed = 1609.34 / speed;
                int secondsSpeed = (int)(newSpeed%60);
                String theSpeed;
                newSpeed = (int)(newSpeed/60);
                //if(newSpeed > 60)
                    speedTextDialog.setText(metersPerSecondToMinutesPerKilometer(speed));
                //else if((secondsSpeed + "").length() == 1) {
                    //theSpeed = (int) (newSpeed) + ":0" + secondsSpeed;
                    //speedTextDialog.setText(theSpeed + " m/mi");
                //}
                //else {
                //    theSpeed = (int) (newSpeed) + ":" + secondsSpeed;
                //    speedTextDialog.setText(theSpeed + " m/mi");
               // }

                Log.d(TAG, "\nLat: " + lat + "\nLong" + lon + "\nSpeed: " + speed
                        + "\nAccuracy: " + accuracy);

                if (MapFragment.currentlyTrackingLocation){
                    bearingArr.add(bearing + "");
                    speedArr.add(speed + "");
                    //timeArr.add(time + "");
                    int size = bearingArr.size();
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
                    int speedSize = speedArr.size();
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


                    mapPlotter.addMarker(lat, lon);
                    new LocationObject(context, uid, lat, lon, speed, bearing, altitude, time).uploadToFirebase();
                    Log.d(TAG, "Uploading to Firebase");
                    latLngList.add(new LatLng(lat, lon));

                    TrackPoint temp = new TrackPoint();
                    //if(accuracy<16)
                    trackPoints.add(temp);

                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
                    df.setTimeZone(tz);
                    String nowAsISO = df.format(new Date());

                    XMLCreator.addPoint(lat, lon, altitude, 69, nowAsISO);


                    if(bearingArr.size() <2) {distance = 0;}
                    else {
                        distance += calculationByDistance(prevLat*Math.PI/180,prevLon*Math.PI/180,
                                lat*Math.PI/180, lon*Math.PI/180);
                        //distance += distanceBetweenTwoCoordinates(prevLat,prevLon,
                        // lat, lon);}
                    }
                    Log.d("Distance text", distance + "");
                    int tempDistance = (int) distance;
                    distanceDialog.setText(tempDistance + "");

                    // Calculate pace


                    prevLat = lat;
                    prevLon = lon;
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
                .setInterval(1000)
                .setFastestInterval(1000)
                .setSmallestDisplacement(0);

        return locationRequest;

    }

    public void setCurrentRunningTime(int time){
        this.time = time;
    }

   // public double getTheSpeed() {
        //return speed;
    //}

    public double calculationByDistance(double initialLat, double initialLong, double finalLat, double finalLong){
        /*PRE: All the input values are in radians!*/

        double latDiff = finalLat - initialLat;
        double longDiff = finalLong - initialLong;
        double earthRadius = 6371; //In Km if you want the distance in km

        double distanceTemp = 1000*2*earthRadius*Math.asin(Math.sqrt(Math.pow(Math.sin(latDiff/2.0),2)+Math.cos(initialLat)*Math.cos(finalLat)*Math.pow(Math.sin(longDiff/2),2)));

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

    private String metersPerSecondToMinutesPerKilometer(double speed) {

        if (speed != 0) {
            double pace = (1 / speed) / 60 * 1000;
            System.out.println(pace);

            int minutes = (int) Math.floor(pace);
            System.out.println(minutes);

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
}
