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
import java.util.HashMap;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
    DatabaseReference databaseReference;
    GoogleMap googleMapGlobal;
    LatLng home;
    List<Marker> markers = new ArrayList<>();
    Polyline currentPolyLine;
    List<Polyline> polyLines = new ArrayList<>();

    // Identifies fine location permission
    private static final int ACCESS_FINE_LOCATION = 1;

    // Gets a unique hardware ID for a device
    @SuppressLint("HardwareIds")
    private String getID() {
        return Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Removes the top bar on top of the map
        getSupportActionBar().hide();

        // Ideally we would want this to be the location one is at when they start the app
        home = new LatLng(37.4419, -122.1430);

        // It will help to look at the Firebase DB. This gets a reference to the
        // 'coords' directory I've made
        databaseReference = FirebaseDatabase.getInstance().getReference().child("coords");

        // Defines a 'fragment' of the activity dedicated to the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


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

        // Defines a location service
        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(LOCATION_SERVICE);

        // See below LocationListener class
        locationListener = new MyLocationListener();

        // This listens for any 'change' in the child that's been selected ('coords')
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                // This takes the data from the database and converts it into a Java object
                HashMap data = (HashMap) dataSnapshot.getValue();

                // Gets individual values from the HashMap
                assert data != null;
                double lat = (double) data.get("lat");
                double lon = (double) data.get("long");
                // double time = (double) data.get("time"); // Not used yet

                LatLng currentLocation = new LatLng(lat, lon);

                // Image file
                BitmapDescriptor currentLocationIcon = fromResource(R.drawable.current);
                BitmapDescriptor beachFlag = fromResource(R.drawable.beachflag);


                // Adds a new marker on the LOCAL map. (The one on the website is written elsewhere).
                Marker newMarker = googleMapGlobal.addMarker(new MarkerOptions()
                        .position(currentLocation)
                        .icon(currentLocationIcon));


                if(markers.size() > 0){

                    LatLng previousLocation = markers.get(markers.size() - 1).getPosition();

                    currentPolyLine = googleMapGlobal.addPolyline(new PolylineOptions()
                            .add(previousLocation, currentLocation)
                            .width(5)
                            .color(Color.RED));

                    polyLines.add(currentPolyLine);
                }

                // Add the marker to an array containing all markers
                markers.add(newMarker);

                // This makes sure only the most recent marker has the 'current' icon
                if (markers.size() > 0){

                    for (int i = 0; i < markers.size() - 1; i++){

                        // markers.get(i).setIcon(beachFlag);
                        markers.get(i).setVisible(false);

                    }
                }

                float zoom = 14;
                googleMapGlobal.moveCamera(CameraUpdateFactory
                        .newLatLngZoom(currentLocation, zoom));


            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                // Testing again. If children are removed, take everything off of the map
                googleMapGlobal.clear();

                // Remove all markers
                markers = new ArrayList<>();

                polyLines = new ArrayList<>();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        // Attaches the above listener to the DB reference
        databaseReference.addChildEventListener(childEventListener);

        // Unsure which minTime and minDistance values work best
        // minTime is in milliseconds, distance in meters
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager
                .GPS_PROVIDER, 1000, 0, locationListener);


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

// This class deals with POSTing the data to Firebase
class PostData extends AsyncTask<String, Void, Void> {

    // Init. client and data type
    private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    private OkHttpClient client = new OkHttpClient();

    protected void onPreExecute() {
        //display progress dialog.

    }

    protected Void doInBackground(String... strings) {

        // Gets the json string from the parameters
        String jsonData = strings[0];

        // Builds a request then POSTs to Firebase
        RequestBody body = RequestBody.create(JSON, jsonData);
        Request request = new Request.Builder()
                .url("https://gps-app-c31df.firebaseio.com/coords.json")
                .post(body)  // Changing this from put to post changes behavior
                .build();

        try {

            // Should find out what to do with response
            Response response = client.newCall(request).execute();

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected void onPostExecute(Void result) {

    }

}

class MyLocationListener implements LocationListener {

    // Takes location data and turns it into JSON-like string
    private String parseGPSData(Location location) {
        double lat = location.getLatitude();
        double lon = location.getLongitude();
        double time = location.getTime();

        return "{\"lat\": " + lat + ", \"long\": " + lon + ", \"time\": " + time + "}";
    }

    public void onLocationChanged(Location location) {

        // Logcat message
        String message = String.format(
                "New Location \n Longitude: %1$s \n Latitude: %2$s",
                location.getLongitude(), location.getLatitude()
        );

        // Parse data, then POST using PostData class
        String data = parseGPSData(location);
        PostData post = new PostData();
        post.execute(data);

        Log.v("GPSDATA", message);
    }

    public void onStatusChanged(String provider, int status, Bundle extras) {

        switch (status) {
            case LocationProvider.OUT_OF_SERVICE:
                Log.v("GPSDATA", "OUT_OF_SERVICE");
                break;
            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                Log.v("GPSDATA", "TEMPORARILY_UNAVAILABLE");
                break;
            case LocationProvider.AVAILABLE:
                Log.v("GPSDATA", "AVAILABLE");
                break;
        }
    }

    public void onProviderDisabled(String provider) {
        Log.v("GPSDATA", "Disabled");
    }

    public void onProviderEnabled(String provider) {
        Log.v("GPSDATA", "Enabled");
    }

}
