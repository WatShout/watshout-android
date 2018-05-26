package com.watshout.face;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


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

    // Location objects
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    ArrayList<String> requestIDs = new ArrayList<>();

    ArrayList<Marker> markerList;
    MapPlotter mapPlotter;

    // General database reference
    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    // Not sure which of these is better
    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();

    String uid = thisUser.getUid();
    String email = thisUser.getEmail();
    String name = thisUser.getDisplayName();

    GoogleMap googleMapGlobal;
    LatLng home;

    String CURRENT_DEVICE_ID;

    Boolean isMapMoving;

    static boolean GPSconnected = false;

    PopupWindow popupWindow;
    LayoutInflater layoutInflater;

    RelativeLayout mRelativeLayout;

    // Resource file declarations
    Button mCurrent;
    Button mZoom;
    Button mSignOut;
    Button mAddFriend;
    Button mViewFriends;
    TextView mGreeting;

    private Old_MyNotificationManager oldMyNotificationManager;

    @SuppressLint("StaticFieldLeak")  // Note: eventually fix this static leak
    static TextView gpsStatus;
    @SuppressLint("StaticFieldLeak")
    static TextView mSpeed;
    @SuppressLint("StaticFieldLeak")
    static TextView mBearing;

    // Identifies fine location permission
    private static final int ACCESS_FINE_LOCATION = 1;

    // Gets a unique hardware ID for a device
    @SuppressLint("HardwareIds")
    String getDeviceID() {
        return Secure.getString(getApplicationContext().getContentResolver(), Secure.ANDROID_ID);
    }

    // This runs as the map rendering is completed
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // IMPORTANT
        // This ensures that we can make changes to the map outside of this function
        // which is why we defined it globally
        googleMapGlobal = googleMap;

        // This sets the starting zoom level
        float zoom = 16;

        // This sets the initial view of the map
        // 'home' is declared earlier
        googleMapGlobal.moveCamera(CameraUpdateFactory.newLatLngZoom(home, zoom));



        // Marker list is a array of the current user's Markers
        markerList = new ArrayList<>();


        mapPlotter = new MapPlotter(markerList, googleMapGlobal);

        // Starts location-getting process
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        FusedLocation fusedLocation = new FusedLocation(getApplicationContext(), mapPlotter);
        locationRequest = fusedLocation.buildLocationRequest();
        locationCallback = fusedLocation.buildLocationCallback();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    public void notifyUser(String from, String notification) {

        oldMyNotificationManager = new Old_MyNotificationManager(getApplicationContext());

        oldMyNotificationManager.showNotification(
                from,
                notification,
                new Intent(getApplicationContext(), MainActivity.class)
        );

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkPermissions();

        // This helps the app not crash in certain contexts
        MapsInitializer.initialize(getApplicationContext());

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel mChannel =
                    new NotificationChannel(Constants.CHANNEL_ID, Constants.CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);

            mChannel.setDescription(Constants.CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);
            mChannel.setLightColor(Color.RED);
            mChannel.enableVibration(true);
            mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});

            mNotificationManager.createNotificationChannel(mChannel);

        }

        mCurrent = findViewById(R.id.current);
        mSignOut = findViewById(R.id.signout);
        mGreeting = findViewById(R.id.greeting);
        mAddFriend = findViewById(R.id.addfriend);
        mViewFriends = findViewById(R.id.viewFriends);

        String greetingText = "Hello, " + email;

        mGreeting.setText(greetingText);

        mRelativeLayout = findViewById(R.id.relative);

        isMapMoving = true;

        // Defines a 'fragment' of the activity dedicated to the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        // Removes the top bar on top of the map
        getSupportActionBar().hide();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int displayHeight = displayMetrics.heightPixels;
        final int displayWidth = displayMetrics.widthPixels;


        notifyUser("test", "test");


        // I know global variables are bad but I have no clue how else to do this
        CURRENT_DEVICE_ID = getDeviceID();
        CurrentID.setCurrent(CURRENT_DEVICE_ID);

        // Right now, the app will delete any old location data when it starts
        // Obviously this is not permanent.
        ref.child("devices").child(CURRENT_DEVICE_ID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                dataSnapshot.getRef().removeValue();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        // This is the initial check to see if a user is 'new' or not
        ref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (!dataSnapshot.exists()) {

                    layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

                    assert layoutInflater != null;
                    ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup, null);

                    popupWindow = new PopupWindow(container, displayWidth, displayHeight, true);
                    popupWindow.showAtLocation(mRelativeLayout, Gravity.NO_GRAVITY, 0, 0);

                    final EditText mAge = container.findViewById(R.id.age);

                    Button mButton = container.findViewById(R.id.submitinfo);
                    mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            int age = Integer.parseInt(mAge.getText().toString());

                            ref.child("users").child(uid).child("name").setValue(name);
                            ref.child("users").child(uid).child("age").setValue(age);
                            ref.child("users").child(uid).child("email").setValue(email);
                            ref.child("users").child(uid).child("device").setValue(CURRENT_DEVICE_ID);

                            popupWindow.dismiss();
                        }
                    });


                } else {
                    ref.child("users").child(uid).child("device").setValue(CURRENT_DEVICE_ID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Ideally we would want this to be the location one is at when they start the app
        home = new LatLng(37.4419, -122.1430);

        // Sets current location
        mCurrent.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {

                Log.wtf("IDS", requestIDs.toString());

                if (GPSconnected) {

                    mapPlotter.moveCamera(16);

                }
            }
        });

        mSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AuthUI.getInstance()
                        .signOut(getApplicationContext())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            public void onComplete(@NonNull Task<Void> task) {
                                // user is now signed out

                                Intent openSignIn = new Intent(getApplicationContext(), SignInActivity.class);
                                getApplicationContext().startActivity(openSignIn);
                                finish();
                            }
                        });
            }
        });

        mAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

                assert layoutInflater != null;
                ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.add_friend, null);


                popupWindow = new PopupWindow(container, displayWidth, displayHeight, true);
                popupWindow.showAtLocation(mRelativeLayout, Gravity.NO_GRAVITY, 0, 0);

                final EditText mEmail = container.findViewById(R.id.email);

                Button mButton = container.findViewById(R.id.request_friend);
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final String theirEmail = mEmail.getText().toString().toLowerCase().replaceAll("\\s+", "");

                        ref.child("users").orderByChild("email").equalTo(theirEmail).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                                    String theirID = childSnapshot.getKey();

                                    ref.child("friend_requests").child(theirID).child(uid).child("request_type")
                                            .setValue("sent");
                                    ref.child("friend_requests").child(uid).child(theirID).child("request_type")
                                            .setValue("received");

                                    Toast.makeText(getApplicationContext(), "Request sent!", Toast.LENGTH_SHORT).show();

                                    popupWindow.dismiss();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });
            }
        });



        // TODO: Turn this into push notification
        ref.child("friend_requests").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                String theirID = dataSnapshot.getKey();
                String requestType = "";

                requestIDs.add(theirID);

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    requestType = (String) child.getValue();
                }

                assert requestType != null;
                if (requestType.equals("received")) {

                    Toast.makeText(getApplicationContext(), theirID + " wants to be your friend", Toast.LENGTH_SHORT).show();

                } else if (requestType.equals("sent")) {

                    // Do nothing

                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

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

                    checkPermissions();

                }
            }
        }
    }

    public void checkPermissions() {
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
    }
}
