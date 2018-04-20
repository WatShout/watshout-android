package com.watshout.face;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings.Secure;
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
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static com.google.android.gms.maps.model.BitmapDescriptorFactory.fromResource;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{

    LocationListener locationListener;
    DatabaseReference databaseReference;
    GoogleMap googleMapGlobal;

    // Identifies fine location permission
    private static final int ACCESS_FINE_LOCATION = 1;

    @SuppressLint("HardwareIds")
    private String getID() {
        return Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("coords");


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        LocationManager locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);

        // See below LocationListener class
        locationListener = new MyLocationListener();

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                HashMap data = (HashMap) dataSnapshot.getValue();

                assert data != null;
                double lat = (double) data.get("lat");
                double lon = (double) data.get("long");
                double time = (double) data.get("time");

                String stringTime = Double.toString(time);

                char timeChar = stringTime.charAt(8);

                int timeInt = (int) timeChar;

                BitmapDescriptor ic;

                if(timeInt % 2 == 0){
                    ic = fromResource(R.drawable.beachflag);
                } else {
                    ic = fromResource(R.drawable.blueflag);
                }

                Marker newMarker = googleMapGlobal.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .icon(ic));

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                googleMapGlobal.clear();

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        databaseReference.addChildEventListener(childEventListener);

        // Unsure which minTime and minDistance values work best
        assert locationManager != null;
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);


    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        googleMapGlobal = googleMap;

        LatLng latLng = new LatLng(-117, 30);
        float zoom = 1;

        googleMapGlobal.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

    }
}

class PostData extends AsyncTask<String, Void, Void> {

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

        // Parse data, then POST
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
