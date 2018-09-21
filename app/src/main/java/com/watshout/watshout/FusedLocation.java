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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.alternativevision.gpx.beans.TrackPoint;
import org.alternativevision.gpx.beans.Waypoint;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
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
    ArrayList<Double> preLat;
    ArrayList<Double> preLon;
    //double speed;
    double prevLat;
    double prevLon;
    double distance;

    public int name;

    boolean out;
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

    GoogleMap googleMap;

    private final static String TAG = "FusedLocation";

    FusedLocation(Context context, MapPlotter mapPlotter, String uid,
                  XMLCreator XMLCreator, TextView speedTextDialog,
                  TextView stepsDialog, TextView distanceDialog, ArrayList preLat, ArrayList preLon,
                  GoogleMap googleMap)
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
        this.preLat = preLat;
        this.preLon = preLon;
        distance = 0;
        out = false;
        this.googleMap = googleMap;

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
                double bearing = location.getBearing();
                double altitude = location.getAltitude();
                long time = location.getTime();
                float accuracy = location.getAccuracy();

                // Center map on current location
                LatLng now = new LatLng(lat, lon);
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(now));

                speedTextDialog.setText(metersPerSecondToMinutesPerKilometer(speed, false));

                Log.d(TAG, "\nLat: " + lat + "\nLong" + lon + "\nSpeed: " + speed
                        + "\nAccuracy: " + accuracy);

                if (MapFragment.currentlyTrackingLocation){

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    int usedName = preferences.getInt("fusedLocationName", -1);

                    if (name == usedName) {
                        bearingArr.add(bearing + "");
                        speedArr.add(speed + "");

                        if(out == false && preLat!= null) {
                            for(int x = 0; x < preLat.size(); x ++)
                                mapPlotter.addMarker(preLat.get(x), preLon.get(x));
                            out = true;
                        }

                        mapPlotter.addMarker(lat, lon);

                        new LocationObject(context, uid, lat, lon, speed, bearing, altitude, time).uploadToFirebase();
                        Log.d(TAG, "Uploading to Firebase");
                        Log.d("MEM_LOCATION", name + "");
                        latLngList.add(new LatLng(lat, lon));

                        TimeZone tz = TimeZone.getTimeZone("UTC");
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
                        df.setTimeZone(tz);
                        String nowAsISO = df.format(new Date());

                        XMLCreator.addPoint(lat, lon, altitude, 69, nowAsISO);
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
                .setInterval(1000)
                .setFastestInterval(1000)
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

    private String metersPerSecondToMinutesPerKilometer(double speed, boolean metric) {

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

   /* public boolean method() {

    }*/

}
