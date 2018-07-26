package com.watshout.watshout;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.TextView;

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
    double speed;
    double prevLat;
    double prevLon;
    double distance;
    int counter;
    TextView speedTextDialog;
    TextView stepsDialog;
    TextView distanceDialog;

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

    }

    public LocationCallback buildLocationCallback() {

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                MapFragment.GPSconnected = true;

                Location location = locationResult.getLocations().get(0);

                latitude = location.getLatitude();
                longitude = location.getLongitude();

                double lat = location.getLatitude();
                double lon = location.getLongitude();
                speed = location.getSpeed();
                double bearing = location.getBearing();
                double altitude = location.getAltitude();
                long time = location.getTime();

                float accuracy = location.getAccuracy();

                Log.wtf("GPSACCURACY", accuracy + "");

                mapPlotter.addMarker(lat, lon);
                double newSpeed = 1609.34/speed;
                int secondsSpeed = (int)(newSpeed%60);
                String theSpeed;
                newSpeed = (int)(newSpeed/60);
                if(newSpeed > 60)
                    speedTextDialog.setText("too slow");
                else if((secondsSpeed + "").length() == 1) {
                    theSpeed = (int) (newSpeed) + ":0" + secondsSpeed;
                    speedTextDialog.setText(theSpeed + " min/mile");
                }
                else {
                    theSpeed = (int) (newSpeed) + ":" + secondsSpeed;
                    speedTextDialog.setText(theSpeed + " min/mile");
                }


                Log.wtf("GPSGPSGPS", "Lat: " + lat + "\nLong" + lon + "\nTracking: " +"Speed: " + speed + MapFragment.currentlyTrackingLocation);

                if (MapFragment.currentlyTrackingLocation){
                    Log.d("TRACKING", "Currently uploading a location object");
                    new LocationObject(context, uid, lat, lon, speed, bearing, altitude, time).uploadToFirebase();

                    TrackPoint temp = new TrackPoint();
                    //if(accuracy<16)
                    trackPoints.add(temp);

                    TimeZone tz = TimeZone.getTimeZone("UTC");
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
                    df.setTimeZone(tz);
                    String nowAsISO = df.format(new Date());

                    XMLCreator.addPoint(lat, lon, altitude, 69, nowAsISO);
                    if(counter == 0) {}
                    else {
                        distance += calculationByDistance(prevLat*Math.PI/180,prevLon*Math.PI/180,
                                lat*Math.PI/180, lon*Math.PI/180 );
                        System.out.println("distance is " + distance);
                        int tempDistance = (int) distance;
                        distanceDialog.setText(tempDistance + " meters");
                    }
                    counter ++;
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
                .setSmallestDisplacement(5);

        return locationRequest;

    }

    public ArrayList<Waypoint> getTrackPoints() {
        return trackPoints;
    }

    public double getTheSpeed() {
        return speed;
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
}
