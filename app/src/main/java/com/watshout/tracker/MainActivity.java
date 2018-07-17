package com.watshout.tracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.provider.MediaStore;
import android.provider.Settings.Secure;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;


import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;

import org.apache.log4j.chainsaw.Main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import com.google.firebase.storage.StorageReference;


import static com.google.android.gms.maps.model.BitmapDescriptorFactory.fromBitmap;


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

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener {

    final long TEN_MEGABYTE = 10 * 1024 * 1024;

    DrawerLayout mDrawerLayout;
    MapFragment mapFragment = new MapFragment();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String email = thisUser.getEmail();
    String uid = thisUser.getUid();

    private StorageReference mStorageRef;
    private DatabaseReference activityImagesRef;

    String fileType;


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

    ArrayList<String> imageNames;

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
    public void onMapReady(final GoogleMap googleMap) {

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

        // Firebase for getting the images from storage, using a listener for changes in the database that is updated simultaneously as the storage
        mStorageRef = FirebaseStorage.getInstance().getReference();

        DatabaseReference rootDatabaseRef = FirebaseDatabase.getInstance().getReference();
        activityImagesRef = rootDatabaseRef.child("users").child(uid).child("activityImages");
        imageNames = new ArrayList<>();

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) { // Firebase

                imageNames = new ArrayList<>();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    String friend = ds.getKey();
                    if (ds.getValue(boolean.class)) {
                        imageNames.add(friend);
                    }
                }
                // Plotting map markers for taken pictures
                Log.e("TEST", imageNames.toString());
                if (imageNames != null) {
                    if (imageNames.size() != 0) { // Making sure there are images in the database
                        for (int currImgNum = 0; currImgNum < imageNames.size(); currImgNum++) {
                            // Getting metadata
                            final StorageReference imageRef = mStorageRef.child("users/" + uid+"/activityImages/"+imageNames.get(currImgNum));
                            imageRef.getBytes(1024*1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    final Bitmap currentBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                    imageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                        @Override
                                        public void onSuccess(StorageMetadata storageMetadata) {
                                            String imageLatLong = storageMetadata.getCustomMetadata("location");
                                            double latitude = Double.parseDouble(imageLatLong.split(",")[0]);
                                            double longitude = Double.parseDouble(imageLatLong.split(",")[1]);
                                            //mapPlotter.addMarker(latitude, longitude);
                                            googleMapGlobal.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(fromBitmap(currentBitmap)));
                                            Log.e("Marker", latitude + " , " + longitude);
                                        }
                                    });
                                }
                            });
                        }
                    }
                }
                else {
                    Toast.makeText(getApplicationContext(), "Error plotting markers for taken images", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Do nothing
            }
        };
        activityImagesRef.addValueEventListener(eventListener);

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
        navigationView.setNavigationItemSelectedListener(this);

        final View navView = navigationView.inflateHeaderView(R.layout.nav_header);
        TextView mEmail = navView.findViewById(R.id.nav_head_email);
        mEmail.setText(email);

        ref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Log.d("USER", dataSnapshot.exists() + "");

                if (!dataSnapshot.exists()) {

                    layoutInflater = (LayoutInflater) getApplicationContext().getSystemService(LAYOUT_INFLATER_SERVICE);

                    assert layoutInflater != null;
                    ViewGroup container = (ViewGroup) layoutInflater.inflate(R.layout.popup, null);

                    popupWindow = new PopupWindow(container, displayWidth, displayHeight, true);
                    popupWindow.showAtLocation(mDrawerLayout, Gravity.NO_GRAVITY, 0, 0);

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

                if (!dataSnapshot.hasChild("profile_pic_format")) {
                    Intent openPfp = new Intent(getApplicationContext(), RequireProfilePictureActivity.class);
                    openPfp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(openPfp);
                    finish();
                } else {
                    fileType = (String) dataSnapshot.child("profile_pic_format").getValue();


                    final ImageView mProfile = navView.findViewById(R.id.profilePic);
                    storageReference.child("users").child(uid).child("profile." + fileType).getBytes(TEN_MEGABYTE)
                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {

                            Log.d("MAIN", "Success download");

                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mProfile.setImageBitmap(bmp);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                            Log.e("MAIN", exception.toString());
                        }
                    });
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

        Button cameraButton = (Button) findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
                intent.putExtra("uid", uid);
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

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.screen_area, mapFragment)
                .commit();

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // set item as selected to persist highlight
        int id = menuItem.getItemId();

        if (id == R.id.nav_news_feed) {

            Log.e("NEWS", "news feed");

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.screen_area, new NewsFeedFragment())
                    .commit();

        } else if (id == R.id.nav_home) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.screen_area, mapFragment)
                    .commit();

        }
        else if (id == R.id.nav_calendar) {
            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.screen_area, new CalendarFragment())
                    .commit();

        }
        else if (id == R.id.nav_settings) {

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.screen_area, new SettingsFragment())
                    .commit();

        } else if (id == R.id.nav_signout) {

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.screen_area, new SignOutFragment())
                    .commit();

        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
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
}
