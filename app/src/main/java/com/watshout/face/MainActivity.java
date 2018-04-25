package com.watshout.face;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.fromResource;
import static com.watshout.face.MainActivity.gpsStatus;


/*
===================================
Welcome to the GPS app source code!

I'm going to try to document this as
well as I can, because it's very easy
to get lost.

The main thing to understand is the
implemented methods for various methods
like the maps and Firebase things, etc.

====================================
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    // Globally-defined
    LocationListener locationListener;
    LocationManager locationManager;
    DatabaseReference deviceSpecificDatabaseReference;
    DatabaseReference allDevicesDatabaseReference;
    GoogleMap googleMapGlobal;
    LatLng home;

    List<Marker> myMarkers = new ArrayList<>();
    List<Polyline> myPolyLines = new ArrayList<>();

    List<Marker> theirMarkers = new ArrayList<>();
    List<Polyline> theirPolyLines = new ArrayList<>();

    Polyline myCurrentPolyLine;
    Polyline theirCurrentPolyLine;

    String CURRENT_ID;

    static TextView gpsStatus;

    // Identifies fine location permission
    private static final int ACCESS_FINE_LOCATION = 1;

    // Gets a unique hardware ID for a device
    @SuppressLint("HardwareIds")
    String getID() {
        return Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Honestly I copied this from StackOverflow. It gets the GPS permissions. Don't mess with
        // it! :D
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
                // Async error

            } else {

                // No explanation needed; request the permission
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        ACCESS_FINE_LOCATION);
            }
        }

        gpsStatus = findViewById(R.id.gps);

        // Removes the top bar on top of the map
        getSupportActionBar().hide();

        // Ideally we would want this to be the location one is at when they start the app
        home = new LatLng(37.4419, -122.1430);

        CURRENT_ID = getID();

        CurrentID.setCurrent(CURRENT_ID);

        // It will help to look at the Firebase DB. This gets a reference to the
        // 'coords' directory I've made
        deviceSpecificDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(CurrentID.getCurrent());

        allDevicesDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference();


        // Defines a 'fragment' of the activity dedicated to the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Defines a location service
        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(LOCATION_SERVICE);

        // See below LocationListener class
        locationListener = new MyLocationListener();

        // This listens for any 'change' in the child that's been selected ('coords')
        ChildEventListener specificChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String justAddedID = dataSnapshot.getRef().getParent().getKey();

                // This takes the data from the database and converts it into a Java object
                HashMap data = (HashMap) dataSnapshot.getValue();

                // Gets individual values from the HashMap
                assert data != null;
                double lat = (double) data.get("lat");
                double lon = (double) data.get("long");
                // double time = (double) data.get("time"); // Not used yet

                String printMe = ": " + justAddedID + "\nLat: " + Double.toString(lat) + "\nLong: " + Double.toString(lon);

                Log.wtf("GPS", "SINGLE ADDED" + printMe);

                LatLng currentLocation = addMarker(lat, lon, myMarkers, myPolyLines,
                        Color.RED);

                float zoom = 1;
                googleMapGlobal.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(currentLocation, zoom));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                String justAddedID = dataSnapshot.getRef().getParent().getKey();

                Log.wtf("GPS", "SINGLE CHANGED" + justAddedID);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                // Testing again. If children are removed, take everything off of the map
                googleMapGlobal.clear();

                // Remove all myMarkers
                myMarkers = new ArrayList<>();

                myPolyLines = new ArrayList<>();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        ChildEventListener everyChildEventListener = new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                String justAddedID = dataSnapshot.getRef().getKey();

                ArrayList<Double> lats = new ArrayList<>();
                ArrayList<Double> longs = new ArrayList<>();

                if (!justAddedID.equals(CURRENT_ID)) {

                    HashMap data = (HashMap) dataSnapshot.getValue();

                    assert data != null;
                    Set children = data.keySet();

                    for (Object child : children){

                        HashMap currentData = (HashMap) dataSnapshot.child(child.toString()).getValue();

                        assert currentData != null;
                        double lat = (double) currentData.get("lat");
                        double lon = (double) currentData.get("long");

                        String printMe = ": " + justAddedID + "\nLat: " + Double.toString(lat) + "\nLong: " + Double.toString(lon);
                        Log.wtf("GPS", "EVERY ADDED" + printMe);

                        lats.add(lat);
                        longs.add(lon);


                        LatLng currentLocation = addMarker(lat, lon, theirMarkers, theirPolyLines,
                                Color.BLUE);

                    }

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String justAddedID = dataSnapshot.getRef().getKey();

                ArrayList<Double> lats = new ArrayList<>();
                ArrayList<Double> longs = new ArrayList<>();

                if (!justAddedID.equals(CURRENT_ID)) {

                    HashMap data = (HashMap) dataSnapshot.getValue();

                    for (DataSnapshot direction: dataSnapshot.getChildren()) {
                        Log.wtf("GPS", direction.getValue().toString());
                    }

                    assert data != null;
                    Set children = data.keySet();


                    for (Object child : children){

                        HashMap currentData = (HashMap) dataSnapshot
                                .child(child.toString())
                                .getValue();

                        //Log.wtf("GPS", "CURRENT" + currentData.toString());

                        assert currentData != null;
                        double lat = (double) currentData.get("lat");
                        double lon = (double) currentData.get("long");

                        String printMe = ": " + justAddedID + "\nLat: " + Double.toString(lat) + "\nLong: " + Double.toString(lon);
                        //Log.wtf("GPS", "EVERY CHANGED" + printMe);

                        lats.add(lat);
                        longs.add(lon);


                        LatLng currentLocation = addMarker(lat, lon, theirMarkers, theirPolyLines,
                                Color.BLUE);

                    }

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                // Testing again. If children are removed, take everything off of the map
                googleMapGlobal.clear();

                // Remove all myMarkers
                theirMarkers = new ArrayList<>();
                theirPolyLines = new ArrayList<>();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // Attaches the above listener to the DB reference
        deviceSpecificDatabaseReference.addChildEventListener(specificChildEventListener);

        allDevicesDatabaseReference.addChildEventListener(everyChildEventListener);

        // Unsure which minTime and minDistance values work best
        // minTime is in milliseconds, distance in meters
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager
                .GPS_PROVIDER, 5000, 3, locationListener);


    }

    public LatLng addMarker(double lat, double lon, List<Marker> markers,
                          List<Polyline> polylines, int color){

        LatLng currentLocation = new LatLng(lat, lon);

        BitmapDescriptor currentLocationIcon = fromResource(R.drawable.current);

        // Adds a new marker on the LOCAL map. (The one on the website is written elsewhere).
        Marker newMarker = googleMapGlobal.addMarker(new MarkerOptions()
                .position(currentLocation)
                .icon(currentLocationIcon));


        if(markers.size() > 0){

            LatLng previousLocation = markers.get(markers.size() - 1).getPosition();

            Polyline currentPolyLine = googleMapGlobal.addPolyline(new PolylineOptions()
                    .add(previousLocation, currentLocation)
                    .width(5)
                    .color(color));

            polylines.add(currentPolyLine);
        }

        // Add the marker to an array containing all myMarkers
        markers.add(newMarker);

        // This makes sure only the most recent marker has the 'current' icon
        if (markers.size() > 0){

            for (int i = 0; i < markers.size() - 1; i++){

                // myMarkers.get(i).setIcon(beachFlag);
                markers.get(i).setVisible(false);

            }
        }

        return currentLocation;

    }

    // This is from StackOverflow too
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission enabled

                } else {

                    // Permission disabled

                }
            }
        }
    }

    // This runs as the map rendering is completed
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // IMPORTANT
        // This ensures that we can make changes to the map outside of this function
        // which is why we defined it globally
        googleMapGlobal = googleMap;

        // This sets the starting zoom level
        float zoom = 1;

        // This sets the initial view of the map
        // 'home' is declared earlier
        googleMapGlobal.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));

    }
}

