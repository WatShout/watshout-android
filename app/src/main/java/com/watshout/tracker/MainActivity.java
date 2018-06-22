package com.watshout.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
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

import org.apache.log4j.chainsaw.Main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;


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

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        NavigationView.OnNavigationItemSelectedListener {

    DrawerLayout mDrawerLayout;

    // Location objects
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

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
    static boolean currentlyTrackingLocation = false;
    static boolean activityRunning = false;

    XMLCreator XMLCreator;

    PopupWindow popupWindow;
    LayoutInflater layoutInflater;

    RelativeLayout mRelativeLayout;

    // Resource file declarations
    Button mStart;
    Button mLap;
    Button mStop;
    Button mCurrent;
    Button mSignOut;
    Button mAddFriend;
    Button mViewFriends;
    TextView mGreeting;
    TextView mTimerText;
    long originalStartTime;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler handler;
    int Seconds, Minutes, MilliSeconds ;

    private boolean timeRunning = false;

    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};

    // For permissions
    int permCode = 200;

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

        // Marker list is a array of the current user's Markers
        markerList = new ArrayList<>();

        mapPlotter = new MapPlotter(markerList, googleMapGlobal, true);

        try {
            XMLCreator = new XMLCreator(getApplicationContext(), uid);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }

        FriendData friendData = new FriendData(uid, googleMapGlobal);

        // Starts location-getting process
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        FusedLocation fusedLocation = null;
        try {
            fusedLocation = new FusedLocation(getApplicationContext(), mapPlotter, uid, XMLCreator);
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        locationRequest = fusedLocation.buildLocationRequest();
        locationCallback = fusedLocation.buildLocationCallback();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        mapPlotter.moveCamera(zoom);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger);

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        // set item as selected to persist highlight
                        menuItem.setChecked(true);
                        // close drawer when item is tapped
                        mDrawerLayout.closeDrawers();

                        // Add code here to update the UI based on the item selected
                        // For example, swap UI fragments here

                        return true;
                    }
                });

        checkLocationPermissions();

        // This helps the app not crash in certain contexts
        MapsInitializer.initialize(getApplicationContext());

        mStart = findViewById(R.id.start);
        mStop = findViewById(R.id.stop);
        mCurrent = findViewById(R.id.current);
        mSignOut = findViewById(R.id.signout);
        mGreeting = findViewById(R.id.greeting);
        //mAddFriend = findViewById(R.id.addfriend);
        mViewFriends = findViewById(R.id.viewFriends);
        mTimerText = findViewById(R.id.timerText);
        mLap = findViewById(R.id.lap);
        handler = new Handler() ;

        String greetingText = "Hello, " + email;

        mGreeting.setText(greetingText);
        mStart.setBackgroundResource(android.R.drawable.btn_default);

        mRelativeLayout = findViewById(R.id.relative);

        isMapMoving = true;

        // Defines a 'fragment' of the activity dedicated to the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Removes the top bar on top of the map
        //getSupportActionBar().hide();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int displayHeight = displayMetrics.heightPixels;
        final int displayWidth = displayMetrics.widthPixels;

        // I know global variables are bad but I have no clue how else to do this
        CURRENT_DEVICE_ID = getDeviceID();
        CurrentID.setCurrent(CURRENT_DEVICE_ID);

        // This is the initial check to see if a user is 'new' or not
        ref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d("USER", dataSnapshot.exists() + "");

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
                            ref.child("users").child(uid).child("device").child("ID").setValue(CURRENT_DEVICE_ID);

                            popupWindow.dismiss();

                        }
                    });

                    ref.child("users").child(uid).child("device").child("name").setValue(android.os.Build.MODEL);


                } else {
                    ref.child("users").child(uid).child("device").child("ID").setValue(CURRENT_DEVICE_ID);
                    ref.child("users").child(uid).child("device").child("name").setValue(android.os.Build.MODEL);
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
            @Override
            public void onClick(View view) {
                checkLocationPermissions();
                checkLocationPermissions();

                Log.wtf("IDS", requestIDs.toString());

                if (GPSconnected) {

                    mapPlotter.moveCamera(16);

                }
            }
        });

        mLap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (timeRunning){
                    Log.d("TIME", mTimerText.getText().toString());
                    StartTime = SystemClock.uptimeMillis();
                    TimeBuff = 0;
                    handler.postDelayed(runnable, 0);
                } else {

                }
            }
        });

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (timeRunning){
                    TimeBuff += MillisecondTime;
                    handler.removeCallbacks(runnable);
                    timeRunning = false;
                } else {
                    StartTime = SystemClock.uptimeMillis();
                    originalStartTime = StartTime;
                    handler.postDelayed(runnable, 0);
                    timeRunning = true;
                }

                if (!activityRunning){
                    ref.child("users").child(uid).child("device").child("current").addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            dataSnapshot.getRef().removeValue();

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {


                        }

                    });

                    activityRunning = true;
                }

                if (!currentlyTrackingLocation){
                    mStart.setBackgroundColor(Color.GREEN);
                    mStart.setText("PAUSE");
                } else {
                    mStart.setBackgroundResource(android.R.drawable.btn_default);
                    mStart.setText("PLAY");
                }

                currentlyTrackingLocation = !currentlyTrackingLocation;


            }
        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                MillisecondTime = 0L ;
                StartTime = 0L ;
                TimeBuff = 0L ;
                UpdateTime = 0L ;
                Seconds = 0 ;
                Minutes = 0 ;
                MilliSeconds = 0 ;
                handler.removeCallbacks(runnable);
                timeRunning = false;

                mTimerText.setText("0:00");

                mStart.setBackgroundResource(android.R.drawable.btn_default);
                currentlyTrackingLocation = false;
                mapPlotter.clearPolyLines();
                activityRunning  = false;

                // Performs Firebase realtime database operations
                UploadToDatabase uploadToDatabase = new UploadToDatabase(uid);
                uploadToDatabase.moveCurrentToPast();

                String date = uploadToDatabase.getFormattedDate();

                try {
                    XMLCreator.saveFile(date);
                } catch (TransformerException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    XMLCreator.uploadToFirebaseStorage();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                XMLCreator.resetXML();


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

        mViewFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
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

    public Runnable runnable = new Runnable() {

        public void run() {

            MillisecondTime = SystemClock.uptimeMillis() - StartTime;

            UpdateTime = TimeBuff + MillisecondTime;

            Seconds = (int) (UpdateTime / 1000);

            Minutes = Seconds / 60;

            Seconds = Seconds % 60;

            MilliSeconds = (int) (UpdateTime % 1000);

            int milliFirstDigit = Integer.parseInt(Integer.toString(MilliSeconds).substring(0, 1));

            mTimerText.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds));

            handler.postDelayed(this, 0);
        }

    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // Permission enabled

                } else {

                    checkLocationPermissions();

                }
            }
        }
    }

    public void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    permissions,
                    permCode);

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        System.out.println("in here");
        if (id == R.id.nav_home) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else if (id == R.id.nav_music) {

        } else if (id == R.id.nav_friends) {


        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            MainActivity.this.startActivity(intent);
            System.out.println("!!!!!!!!!!!!!");

        }


        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        menuItem.setChecked(true);
                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                });
    }

}
