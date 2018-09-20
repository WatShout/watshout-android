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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.SphericalUtil;
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
    private XMLCreator XMLCreator;
    public static double latitude = 0;
    public static double longitude = 0;
    ArrayList<Double> preLat;
    ArrayList<Double> preLon;
    //double speed;
    double prevLat;
    double prevLon;
    double distance;

    boolean out;
    double info [][] = new double [3][3];
    //double bearingArr [] = new double [3];
    ArrayList<String> bearingArr = new ArrayList<String>();
    ArrayList<String> speedArr = new ArrayList<String>();
    //ArrayList<String> timeArr = new ArrayList<String> ();
    double distance;

    private TextView speedTextDialog;
    private TextView stepsDialog;
    private TextView distanceDialog;
    private SharedPreferences settings;
    private SharedPreferences.Editor editor;
    private List<LatLng> latLngList;

    private double latestLat;
    private double latestLon;

    private LatLng current;
    private LatLng previous;

    private int timesSinceLastPlot = 1;

    private static final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

    private final static String TAG = "FusedLocation";

    FusedLocation(Context context, MapPlotter mapPlotter, String uid,
                  XMLCreator XMLCreator, TextView speedTextDialog,
                  TextView stepsDialog, TextView distanceDialog, ArrayList preLat, ArrayList preLon)
            throws TransformerException, ParserConfigurationException {

        this.context = context;
        this.mapPlotter = mapPlotter;
        this.uid = uid;
        this.XMLCreator = XMLCreator;
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

    }

    public void resetLatLng() {
        this.latLngList = new ArrayList<>();
    }

    public List getLatLng() { return latLngList; }

    private void updateSharedPreferenceValues(double lat, double lon){

        editor.putString("last_latitude", lat + "");
        editor.putString("last_longitude", lon + "");
        editor.commit();

    }


    public LocationCallback buildLocationCallback() {

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                //}


                if (current != null) {
                    previous = current;
                }

                MapFragment.GPSconnected = true;

                Location location = locationResult.getLocations().get(0);

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                latestLat = latitude;
                latestLon = longitude;

                updateSharedPreferenceValues(latitude, longitude);

                double lat = location.getLatitude();
                double lon = location.getLongitude();
                double speed = location.getSpeed();
                double bearing = location.getBearing();
                double altitude = location.getAltitude();
                long time = location.getTime();
                float accuracy = location.getAccuracy();

                speedTextDialog.setText(metersPerSecondToMinutesPerKilometer(speed, false));

                // @ Viraj: Do we still need this?
                //if(newSpeed > 60)

                /*
                double newSpeed = 1609.34 / speed;
                int secondsSpeed = (int)(newSpeed%60);
                String theSpeed;
                newSpeed = (int)(newSpeed/60);
                */

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
                    if(out == false && preLat!= null) {
                        for(int x = 0; x < preLat.size(); x ++)
                            mapPlotter.addMarker(preLat.get(x), preLon.get(x));
                        out = true;
                    }

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

                current = new LatLng(lat, lon);

                if (MapFragment.currentlyTrackingLocation) {

                    if (timesSinceLastPlot == 25) {
                        mapPlotter.addMarker(lat, lon);
                        new LocationObject(context, uid, lat, lon, speed, bearing, altitude, time).uploadToFirebase();
                        latLngList.add(current);
                    }

                    if (previous != null) {
                        Log.d("DISTANCE", "" + SphericalUtil.computeDistanceBetween(previous, current));
                    } else {
                        Log.d("DISTANCE", "Previous is null");
                    }

                    // Every point gets added to GPX file
                    XMLCreator.addPoint(lat, lon, altitude, 0, getCurrentTime());
                }
            }

            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) { }
        };

        return locationCallback;

    }

    private String getCurrentTime() {

        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        String nowAsISO = df.format(new Date());

        return nowAsISO;
    }

    public LocationRequest buildLocationRequest() {

        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(1000)
                .setFastestInterval(1000)
                .setSmallestDisplacement(0);

        return locationRequest;

    }

    public LatLng getLatestLatLng() {
        return new LatLng(latestLat, latestLon);
    }

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

   /* public boolean method() {

    }*/

}
