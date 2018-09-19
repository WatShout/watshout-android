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
                  TextView stepsDialog, TextView distanceDialog)
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
        distance = 0;

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
}
