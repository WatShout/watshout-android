package com.watshout.face;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.fromResource;


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
    String CURRENT_ID;
    List<Marker> myMarkers = new ArrayList<>();
    List<Marker> theirMarkers = new ArrayList<>();

    // Find a better solution for this
    static TextView gpsStatus;
    static TextView speed;

    // Log tags
    final String GPS = "GPSDATA";
    final String DATABASE = "DATABASE";

    // Identifies fine location permission
    private static final int ACCESS_FINE_LOCATION = 1;

    // HashMap that stores all OTHER devices
    HashMap<String, ArrayList> otherDevices = new HashMap();
    List<String> deviceList = new ArrayList<>();

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
        speed = findViewById(R.id.speed);

        Button resize = findViewById(R.id.size);

        // Removes the top bar on top of the map
        getSupportActionBar().hide();

        // Ideally we would want this to be the location one is at when they start the app
        home = new LatLng(37.4419, -122.1430);

        // I know global variables are bad but I have no clue how else to do this
        CURRENT_ID = getID();
        CurrentID.setCurrent(CURRENT_ID);

        // Gets a reference for THIS device
        deviceSpecificDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child(CurrentID.getCurrent());

        // Gets a reference for ALL devices (including this one)
        allDevicesDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference();


        allDevicesDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    deviceList.add(Objects.requireNonNull(childSnapshot.getKey()));
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        for (int i = 0; i < deviceList.size(); i++) {

                /* Dictionary that keeps track of every device
                    deviceDict = {
                                0                1                  2
                    deviceID = [[Marker Values],[Co-Ordinate Value],[Test]]

                    }
                */

            ArrayList<ArrayList> currentLists = new ArrayList<>();

            ArrayList<Marker> markers = new ArrayList<>();
            ArrayList<LatLng> coords = new ArrayList<>();

            currentLists.add(markers);
            currentLists.add(coords);

            otherDevices.put(deviceList.get(i), currentLists);
        }

        // Defines a 'fragment' of the activity dedicated to the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Defines a location service
        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(LOCATION_SERVICE);

        // See below LocationListener class
        locationListener = new MyLocationListener();

        // This listens for any 'change' in the child that's been selected (this specific device)
        ChildEventListener specificChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String justAddedID = dataSnapshot.getRef().getParent().getKey();

                Log.v(DATABASE, "THIS ADDED: " + justAddedID);

                LatLng previousLocation;

                // This takes the data from the database and converts it into a Java object
                HashMap data = (HashMap) dataSnapshot.getValue();

                // Gets individual values from the HashMap
                assert data != null;
                double lat = (double) data.get("lat");
                double lon = (double) data.get("long");

                if (myMarkers.size() > 0) {
                    previousLocation = myMarkers.get(myMarkers.size() - 1).getPosition();
                } else {
                    previousLocation = null;
                }

                addMarker(lat, lon, previousLocation, Color.RED, CURRENT_ID);

                LatLng currentLocation = new LatLng(lat, lon);

                float zoom = 1;
                googleMapGlobal.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(currentLocation, zoom));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String justAddedID = dataSnapshot.getRef().getParent().getKey();

                Log.v(DATABASE, "THIS CHANGED: " + justAddedID);

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                String justAddedID = dataSnapshot.getRef().getParent().getKey();

                Log.v(DATABASE, "THIS REMOVED: " + justAddedID);

                // Testing again. If children are removed, take everything off of the map
                googleMapGlobal.clear();

                // Remove all myMarkers
                myMarkers = new ArrayList<>();

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

                if (!justAddedID.equals(CURRENT_ID)) {

                    Log.v(DATABASE, "THAT ADDED: " + justAddedID);

                    processTheirLocation(dataSnapshot, true, justAddedID);

                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                String justAddedID = dataSnapshot.getRef().getKey();

                if (!justAddedID.equals(CURRENT_ID)) {

                    Log.v(DATABASE, "THAT CHANGED: " + justAddedID);

                    processTheirLocation(dataSnapshot, false, justAddedID);

                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                String justAddedID = dataSnapshot.getRef().getParent().getKey();

                Log.v(DATABASE, "THAT REMOVED: " + justAddedID);

                // Testing again. If children are removed, take everything off of the map
                googleMapGlobal.clear();

                // Remove all myMarkers
                theirMarkers = new ArrayList<>();
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

        // TODO: Figure out best values for these
        // minTime is in milliseconds, distance in meters
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager
                .GPS_PROVIDER, 5000, 3, locationListener);


        resize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                LatLng friendLocation = theirMarkers.get(theirMarkers.size() - 1).getPosition();

                googleMapGlobal.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        friendLocation, 1));
            }
        });


    }

    // This whole function is some voodoo magic.
    public void processTheirLocation(DataSnapshot dataSnapshot, Boolean alreadyExists, String ID) {

        if (!deviceList.contains(ID)) {

            ArrayList<ArrayList> currentLists = new ArrayList<>();

            ArrayList<Marker> markers = new ArrayList<>();
            ArrayList<LatLng> coords = new ArrayList<>();

            currentLists.add(markers);
            currentLists.add(coords);

            otherDevices.put(ID, currentLists);

            deviceList.add(ID);

        }

        LatLng previousLocation;

        ArrayList<DataSnapshot> totalList = new ArrayList<>();

        Iterable<DataSnapshot> iterable = dataSnapshot.getChildren();

        if (iterable != null) {
            for (DataSnapshot ds : iterable) {
                totalList.add(ds);
            }
        }


        ArrayList<Marker> currentTheirMarkers = (ArrayList) otherDevices.get(ID).get(0);

        if (!alreadyExists) {
            HashMap latest = (HashMap) totalList.get(totalList.size() - 1).getValue();

            assert latest != null;
            double lat = (double) latest.get("lat");
            double lon = (double) latest.get("long");
            double speed = (double) latest.get("speed");

            if (currentTheirMarkers.size() > 0) {
                previousLocation = currentTheirMarkers.get(currentTheirMarkers.size() - 1).getPosition();
            } else {
                previousLocation = null;
            }

            addMarker(lat, lon, previousLocation, Color.BLUE, ID);
        } else {

            for (DataSnapshot ds : totalList) {

                HashMap current = (HashMap) ds.getValue();

                assert current != null;
                double lat = (double) current.get("lat");
                double lon = (double) current.get("long");
                double speed = (double) current.get("speed");

                if (currentTheirMarkers.size() > 0) {
                    previousLocation = currentTheirMarkers.get(currentTheirMarkers.size() - 1).getPosition();
                } else {
                    previousLocation = null;
                }

                addMarker(lat, lon, previousLocation, Color.BLUE, ID);

            }
        }
    }


    public void addMarker(double lat, double lon,
                          LatLng previousLocation, int color, String ID) {

        LatLng currentLocation = new LatLng(lat, lon);

        BitmapDescriptor currentLocationIcon = fromResource(R.drawable.current);

        // Adds a new marker on the LOCAL map. (The one on the website is written elsewhere).
        Marker newMarker = googleMapGlobal.addMarker(new MarkerOptions()
                .position(currentLocation)
                .icon(currentLocationIcon));

        if (previousLocation == null) {
            previousLocation = currentLocation;
        }

        if (ID.equals(CURRENT_ID)) {

            myMarkers.add(newMarker);

            //Log.e("GPS", "Previous: " + previousLocation + "\nCurrent: " + currentLocation);

            googleMapGlobal.addPolyline(new PolylineOptions()
                    .add(previousLocation, currentLocation)
                    .width(5)
                    .color(color));


            for (int i = 0; i < myMarkers.size() - 1; i++) {
                myMarkers.get(i).setVisible(false);

            }
        } else {

            ArrayList current = (ArrayList) otherDevices.get(ID).get(0);
            current.add(newMarker);

            if (current.size() > 0) {

                // Log.e("GPS", "Previous: " + previousLocation + "\nCurrent: " + currentLocation);

                googleMapGlobal.addPolyline(new PolylineOptions()
                        .add(previousLocation, currentLocation)
                        .width(5)
                        .color(color));

                Log.e("GPS", "Just drew a line from " + previousLocation + " to " + currentLocation);

            }

            // This makes sure only the most recent marker has the 'current' icon
            if (current.size() > 0) {

                for (int i = 0; i < current.size() - 1; i++) {

                    Marker previousMarker = (Marker) current.get(i);

                    previousMarker.setVisible(false);
                }
            }
        }

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
