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
import java.text.DecimalFormat;
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
    private static MapPlotter mapPlotter;
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
    static TextView distanceDialog;
    SharedPreferences settings;
    SharedPreferences.Editor editor;
    List<LatLng> latLngList;
    int time;

    GoogleMap googleMap;

    private static final double MS_TO_MM = 26.8224;

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

        prevLat = Double.valueOf(settings.getString("last_latitude", "37.4419"));
        prevLon = Double.valueOf(settings.getString("last_longitude", "-122.1430"));

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
                //googleMap.moveCamera(CameraUpdateFactory.newLatLng(now));

                DecimalFormat pace = new DecimalFormat("##");

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
                   double addDistance = 0;
                    if(out == false && preLat!= null) {
                        for(int x = 0; x < preLat.size(); x ++)
                            mapPlotter.addMarker(preLat.get(x), preLon.get(x));
                        out = true;
                        for(int a = 1; a < preLat.size(); a ++){
                            //System.out.println("PREVLAT:" + prevLat + ", PREVLON" + prevLon
                                  //  + ", LAT" + lat + ", LON" + lon);
                          //System.out.println("ADDDISTANCE:" + addDistance);
                            double anssd = calculationByDistance(preLat.get(a-1)*Math.PI/180,preLon.get(a-1)*Math.PI/180,
                                    preLat.get(a)*Math.PI/180, preLon.get(a)*Math.PI/180);
                            System.out.println("12345:" + anssd);

                        distance +=addDistance;
                            System.out.println("GFGFGF:" + addDistance);}

                    }


                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
                    int usedName = preferences.getInt("fusedLocationName", -1);
                    System.out.println("EFEFEF:" + addDistance);
                    if (name == usedName) {
                        bearingArr.add(bearing + "");
                        speedArr.add(speed + "");
                        //System.out.println("USEDNAME!")

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

                    if(prevLat<2 ) {
                        if(distance>0) {
                            System.out.println("If this prints, succesfull");
                        }
                        else
                            distance = 0;
                        }
                    else {
                       System.out.println("INSIDEADDDISTANCESTATEMENT");
                        //System.out.println("PREVLAT:" + prevLat + ", PREVLON" + prevLon
                              //  + ", LAT" + lat + ", LON" + lon);
                        System.out.println("DFDFDF:" + addDistance);
                        distance +=  calculationByDistance(prevLat*Math.PI/180,prevLon*Math.PI/180,
                                lat*Math.PI/180, lon*Math.PI/180);
                        //distance += distanceBetweenTwoCoordinates(prevLat,prevLon,
                        // lat, lon);}
                    }
                    Log.d("Distance text", distance + "");
                    int tempDistance = (int) distance;
                    System.out.println("UNUSUAL:" + tempDistance);
                    double miles = tempDistance * 0.000621371;


                    DecimalFormat decimalFormat = new DecimalFormat("##.##");
                    distanceDialog.setText(decimalFormat.format(miles));
                        TimeZone tz = TimeZone.getTimeZone("UTC");
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
                        df.setTimeZone(tz);
                        String nowAsISO = df.format(new Date());

                        XMLCreator.addPoint(lat, lon, altitude, 0, nowAsISO);
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
