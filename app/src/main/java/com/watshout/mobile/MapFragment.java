package com.watshout.mobile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.PolyUtil;
import com.watshout.mobile.pojo.FriendRequestResponse;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import retrofit2.Call;
import retrofit2.Callback;

public class MapFragment extends android.app.Fragment implements OnMapReadyCallback {

    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    ArrayList<Double> preLat;
    ArrayList<Double> preLon;
    FusedLocation fusedLocation;

    ArrayList<Marker> markerList;
    MapPlotter mapPlotter;
    MapView mv;

    RetrofitInterface retrofitInterface = RetrofitClient
            .getRetrofitInstance().create(RetrofitInterface.class);

    PopupWindow popUp;

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();

    String uid = thisUser.getUid();
    String email = thisUser.getEmail();
    String name = thisUser.getDisplayName();

    int counter;

    GoogleMap googleMapGlobal;
    LatLng home;

    Boolean isMapMoving;

    static boolean GPSconnected = false;
    static boolean currentlyTrackingLocation = false;
    static boolean activityRunning = false;

    RecyclerView mRecyclerView;

    LayoutInflater layoutInflater;

    RelativeLayout mRelativeLayout;

    // Resource file declarations
    Button mStart;
    FloatingActionButton mStop;

    //Button mCamera;

    Button popUpStart;
    FloatingActionButton popUpStop;

    TextView timerText;
    TextView speedTextDialog;
    TextView stepsDialog;
    TextView distanceDialog;

    int numSeconds;
    int numMinutes;
    int numHours;
    long timeGap;
    boolean hasStrava;

    boolean fabShowsData;
    DrawerLayout mDrawerLayout;
    FloatingActionButton mCenter;

    long originalStartTime;

    TextView mTrackingText;
    //ImageView trackerImage;
    //TextView mTracking;

    FloatingActionButton floatingActionButton;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L;
    Handler handler;
    int Hours, Seconds, Minutes, MilliSeconds;

    private boolean timeRunning = false;

    String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};

    // For permissions
    int permCode = 200;

    ImageView mTrackingImageView;

    private final static int CAMERA = 350;

    int secondsAlready;

    int counterTotal = 0;

    public MapFragment() {
        preLat= new ArrayList<>();
        preLon= new ArrayList<>();
    }

    // This runs as the map rendering is completed
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        Log.d("LIFECYCLE", "onMapRead");

        // IMPORTANT
        // This ensures that we can make changes to the map outside of this function
        // which is why we defined it globally
        googleMapGlobal = googleMap;


        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getContext());
        double last_latitude = Double
                .parseDouble(settings.getString("last_latitude", "37.4419"));
        double last_longitude = Double
                .parseDouble(settings.getString("last_longitude", "-122.1430"));

        googleMapGlobal.moveCamera(CameraUpdateFactory
                .newLatLngZoom(new LatLng(last_latitude, last_longitude), 16));

        // This sets the starting zoom level
        float zoom = 16;

        // Marker list is a array of the current user's Markers
        markerList = new ArrayList<>();

        mapPlotter = new MapPlotter(markerList, googleMapGlobal, true, uid, getActivity());


        FriendDataManager friendDataManager = new FriendDataManager(uid, googleMapGlobal, mRecyclerView, getActivity(),
                new MapRecycleViewCarrier(mRecyclerView));


        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());

        try {
            fusedLocation = new FusedLocation(getActivity().getApplicationContext(),
                    mapPlotter, uid, speedTextDialog, stepsDialog, distanceDialog, preLat, preLon,
                    googleMapGlobal);
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        Log.d("MEM_LOCATION", "Just created: " + fusedLocation.name);

        locationRequest = fusedLocation.buildLocationRequest();
        locationCallback = fusedLocation.buildLocationCallback();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        googleMapGlobal.setMyLocationEnabled(true);
        googleMapGlobal.getUiSettings().setMyLocationButtonEnabled(false);

        googleMapGlobal.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.google_map_color
                ));

        mapPlotter.moveCamera(zoom);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Log.d("LIFECYCLE", "onViewCreated");

        mv = view.findViewById(R.id.map);
        mv.onCreate(null);
        mv.onResume();
        mv.getMapAsync(this);

        // add 'currently tracking' in database
        ref.child("users").child(uid).child("currently_tracking").setValue(true);

        mTrackingText = view.findViewById(R.id.tracking_text);
        //mTrackingText.setText("LOCATION SHARING IS NOT ACTIVE");
        //mTracking = view.findViewById(R.id.tracking);
        //mTracking.setVisibility(View.INVISIBLE);
        //trackerImage = view.findViewById(R.id.count_image);
        //trackerImage.setVisibility(View.INVISIBLE);
        mTrackingImageView = view.findViewById(R.id.trackingImageView);
        mTrackingImageView.setColorFilter(getContext().getResources().getColor(R.color.lightBlue));


        ref.child("users").child(uid).child("strava_token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    hasStrava = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // start listener to get number of friends who are currently tracking
        //mTracking.setText(String.format(" COUNT: %d", counterTotal));
        //mTrackingText.setText(String.format("YOUR LOCATION IS BEING SHARED WITH %d FRIENDS", counterTotal));
        mTrackingText.setText("YOUR LOCATION IS NOT BEING SHARED");
        ref.child("friend_data").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {

                    Log.d("FRIENDS", child.getKey());

                    ref.child("users").child(child.getKey())
                            .addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                                    if (dataSnapshot.getKey().equals("currently_tracking")) {

                                        if ((boolean) dataSnapshot.getValue()) {
                                            counterTotal++;
                                            if (activityRunning) {
                                                mTrackingText.setText(String.format("YOUR LOCATION IS BEING SHARED WITH %d FRIENDS", counterTotal));
                                            }
                                        }

                                    }

                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                                    if (dataSnapshot.getKey().equals("currently_tracking")) {

                                        if ((boolean) dataSnapshot.getValue()) {
                                            counterTotal++;
                                            if (activityRunning) {
                                                mTrackingText.setText(String.format("YOUR LOCATION IS BEING SHARED WITH %d FRIENDS", counterTotal));
                                            }
                                        } else {

                                            if (counterTotal > 0) {
                                                counterTotal--;
                                            }
                                            if (activityRunning) {
                                                mTrackingText.setText(String.format("YOUR LOCATION IS BEING SHARED WITH %d FRIENDS", counterTotal));
                                            }
                                        }

                                    }

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot) { }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

                                @Override
                                public void onCancelled(DatabaseError databaseError) { }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();

        getActivity().setTitle("Map");

        mRelativeLayout = view.findViewById(R.id.relative);
        mDrawerLayout = MainActivity.getDrawerLayout();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int displayHeight = displayMetrics.heightPixels;
        final int displayWidth = displayMetrics.widthPixels;
        layoutInflater = (LayoutInflater) getActivity()
                .getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert layoutInflater != null;

        mCenter = view.findViewById(R.id.recenter);
        mRecyclerView = view.findViewById(R.id.friendRecycleView);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View popUpView = inflater.inflate(R.layout.fragment_dialog, null);
        builder.setView(popUpView);
        popUp = new PopupWindow(popUpView, displayWidth, displayHeight, true);

        FloatingActionButton fabDialog = popUpView.findViewById(R.id.location);
        fabDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.fui_slide_out_left);
                final ViewGroup hiddenPanel = (ViewGroup) popUpView.findViewById(R.id.dialogLayout);

                hiddenPanel.startAnimation(bottomUp);
                bottomUp.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationStart(Animation anim) { }
                    public void onAnimationRepeat(Animation anim) { }
                    public void onAnimationEnd(Animation anim) {
                        hiddenPanel.setVisibility(View.INVISIBLE);
                        popUp.dismiss();
                    }
                });
            }
        });

        floatingActionButton = view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.fui_slide_in_right);
                ViewGroup hiddenPanel = (ViewGroup) popUpView.findViewById(R.id.dialogLayout);
                hiddenPanel.startAnimation(bottomUp);
                hiddenPanel.setVisibility(View.VISIBLE);
                popUp.showAtLocation(mRelativeLayout, Gravity.NO_GRAVITY, 0, 0);

            }
        });

        floatingActionButton.hide();

        checkLocationPermissions();

        // This helps the app not crash in certain contexts
        MapsInitializer.initialize(getActivity().getApplicationContext());

        mStart = view.findViewById(R.id.popUpStart);
        mStop = view.findViewById(R.id.stop);
        popUpStop = popUpView.findViewById(R.id.stop);
        popUpStart = popUpView.findViewById(R.id.popUpStart);

        mStop.setVisibility(View.INVISIBLE);
        popUpStop.setVisibility(View.INVISIBLE);
        popUpStop.setBackgroundResource(R.drawable.stop);

        speedTextDialog = popUpView.findViewById(R.id.speedTextDialog);
        stepsDialog = popUpView.findViewById(R.id.stepsDialog);
        distanceDialog = popUpView.findViewById(R.id.distanceDialog);
        timerText = popUpView.findViewById(R.id.timerText1);

        handler = new Handler();

        mStart.setBackgroundResource(R.drawable.round_play_button);

        isMapMoving = true;

        mCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double[] coords = fusedLocation.getLatestCoords();

                double latitude = coords[0];
                double longitude = coords[1];

                LatLng current = new LatLng(latitude, longitude);

                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }

                googleMapGlobal.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 19));

                googleMapGlobal.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.google_map_color
                        ));

            }
        });

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (preferences.getBoolean("currentlyTracking", false)) {

                } else {
                    ref.child("users").child(uid).child("device").child("current")
                            .removeValue();

                    fusedLocation.resetLatLng();
                    mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

                    sendActivityNotification();

                    ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                    actionBar.setDisplayHomeAsUpEnabled(false);
                    actionBar.setHomeButtonEnabled(false);

                    Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                            R.anim.fui_slide_in_right);
                    ViewGroup hiddenPanel = (ViewGroup) popUpView.findViewById(R.id.dialogLayout);
                    hiddenPanel.startAnimation(bottomUp);
                    hiddenPanel.setVisibility(View.VISIBLE);

                    popUp.showAtLocation(mRelativeLayout, Gravity.NO_GRAVITY, 0, 0);
                    startClick();
                }
            }
        });

        popUpStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startClick();
            }
        });

        popUpStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopClick();
            }

        });

        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopClick();
            }

        });

        if (preferences.getBoolean("currentlyTracking", false)) {

            numSeconds = preferences.getInt("numSeconds", 0);
            numMinutes = preferences.getInt("numMinutes", 0);
            numHours = preferences.getInt("numHours", 0);

            long closeTime = preferences.getLong("closeTimeStamp", SystemClock.uptimeMillis());
            long timeRightNow = SystemClock.uptimeMillis();

            timeGap = timeRightNow - closeTime;

            // start timer
            StartTime = SystemClock.uptimeMillis();
            originalStartTime = StartTime;
            handler.postDelayed(runnable, 0);
            timeRunning = true;

            ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setHomeButtonEnabled(false);

            fabDialog.setImageResource(R.drawable.data);
            fabShowsData = true;

            currentlyTrackingLocation = true;
            int resource = R.drawable.round_pause_button;
            mStart.setBackgroundResource(resource);
            popUpStart.setBackgroundResource(resource);

            mStop.setVisibility(View.VISIBLE);
            popUpStop.setVisibility(View.VISIBLE);
            popUpStop.setBackgroundResource(R.drawable.stop);

            floatingActionButton.show();

            updateMapPlotter();

        } else {
            secondsAlready = 0;
        }
    }

    public Runnable runnable = new Runnable() {

        public void run() {

            long addTime = timeGap + (numSeconds * 1000) + (numMinutes * 60000) + (numHours * 3600000);

            MillisecondTime = (System.currentTimeMillis() - StartTime) + addTime;
            UpdateTime = TimeBuff + MillisecondTime;
            Seconds = (int) (UpdateTime / 1000);
            Hours = Seconds / 3600;
            Minutes = (Seconds - (Hours*3600)) / 60;
            Seconds = Seconds % 60;
            MilliSeconds = (int) (UpdateTime % 1000);
            timerText.setText("0" + Hours + ":" + String.format("%02d", Minutes) + ":"
                    + String.format("%02d", Seconds));
            handler.postDelayed(this, 0);
            counter++;
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

                } else {
                    checkLocationPermissions();
                }
            }
        }
    }

    public void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this.getActivity(),
                    permissions,
                    permCode);
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        ref.child("users").child(uid).child("currently_tracking").setValue(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        ref.child("users").child(uid).child("currently_tracking").setValue(true);

    }

    public void startClick() {

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("fusedLocationName", fusedLocation.name);
        editor.apply();

        // LOCATION SHARING IS NOT ACTIVE
        // LOCATION SHARING IS ACTIVE

        mTrackingText.setText(String.format("YOUR LOCATION IS BEING SHARED WITH %d FRIENDS", counterTotal));
        //mTrackingText.setText("LOCATION SHARING IS ACTIVE");
        //trackerImage.setVisibility(View.VISIBLE);
        //mTracking.setVisibility(View.VISIBLE);

        mTrackingImageView.setColorFilter(getContext().getResources().getColor(R.color.lightGreen));

        floatingActionButton.show();
        mStart.setVisibility(View.VISIBLE);
        mStop.setVisibility(View.VISIBLE);
        popUpStop.setVisibility(View.VISIBLE);

        if (timeRunning){
            TimeBuff += MillisecondTime;
            handler.removeCallbacks(runnable);
            timeRunning = false;
        } else {
            StartTime = System.currentTimeMillis();
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
                public void onCancelled(DatabaseError databaseError) { }
            });

            activityRunning = true;
        }

        int resource = currentlyTrackingLocation ? R.drawable.round_play_button : R.drawable.round_pause_button;
        mStart.setBackgroundResource(resource);
        popUpStart.setBackgroundResource(resource);

        currentlyTrackingLocation = !currentlyTrackingLocation;

    }

    public void stopClick() {

        Log.d("MAP", "test");
        mTrackingText.setText("LOCATION SHARING IS NOT ACTIVE");
        //mTracking.setVisibility(View.INVISIBLE);
        //trackerImage.setVisibility(View.INVISIBLE);


        mTrackingImageView.setColorFilter(getContext().getResources().getColor(R.color.lightBlue));
        floatingActionButton.show();
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);

        currentlyTrackingLocation = false;
        activityRunning = false;

        mapPlotter.removeFromMap();

        Log.d("LENGTH", fusedLocation.getLatLng().size() + "");

        ref.child("users").child(uid).child("device").child("current").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<LatLng> latLngList = new ArrayList<>();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    LocationObject locationObject = child.getValue(LocationObject.class);
                    Log.d("LOCATION", "" + locationObject.lat);

                    LatLng current = new LatLng(locationObject.lat, locationObject.lon);
                    latLngList.add(current);

                }

                    DisplayMetrics displayMetrics = new DisplayMetrics();
                    getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                    int height = displayMetrics.heightPixels;
                    int width = displayMetrics.widthPixels;

                    String newSize = "&size=" + 400 + "x" + 640;

                    String encodedPath = PolyUtil.encode(latLngList);
                    String baseMapURL = EndpointURL.getInstance().getCreateMapURL() + encodedPath;
                    String displayMapURL = baseMapURL + "&size=" + 400 + "x" + 640;
                    String uploadMapURL = baseMapURL + "&size=" + 600 + "x" + 300;

                    Intent openNext = new Intent(getActivity().getApplicationContext(), FinishedActivity.class);

                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.running_black);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    byte[] bitmapdata = stream.toByteArray();

                    openNext.putExtra("MAP_IMAGE", bitmapdata);
                    openNext.putExtra("DISPLAY_MAP_URL", displayMapURL);
                    openNext.putExtra("UPLOAD_MAP_URL", uploadMapURL);

                    openNext.putExtra("STRAVA", Boolean.toString(hasStrava));
                    openNext.putExtra("MIN", Minutes);
                    openNext.putExtra("SEC", Seconds);

                    String distance = distanceDialog.getText().toString();
                    openNext.putExtra("DISTANCE", distance);

                    openNext.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getActivity().getApplicationContext().startActivity(openNext);
                    getActivity().finish();

                    // This seems to fix the multi-location updates
                    fusedLocationProviderClient.removeLocationUpdates(locationCallback);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public void sendActivityNotification() {

        Call<FriendRequestResponse> call =
                retrofitInterface.sendActivityNotification(uid);

        call.enqueue(new Callback<FriendRequestResponse>() {
            @Override
            public void onResponse(Call<FriendRequestResponse> call, retrofit2.Response<FriendRequestResponse> response) {
                Log.d("RETRO", "Running notification works");
            }

            @Override
            public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
                Log.d("RETRO", t.toString());
            }
        });
    }

    @Override
    public void onDestroy() {
        // set tracking to false
        ref.child("users").child(uid).child("currently_tracking").setValue(false);
        super.onDestroy();

        Log.d("LIFECYCLE", "onDestroy");

        long closeTimeStamp = System.currentTimeMillis();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(fusedLocation.toString(), "fusedLocationMemoryAddress");
        editor.putLong("closeTimeStamp", closeTimeStamp);

        double[] latestCords = fusedLocation.getLatestCoords();
        double lat = latestCords[0];
        double lon = latestCords[1];

        editor.putString("latestLat", lat + "");
        editor.putString("latestLon", lon + "");
        editor.putBoolean("currentlyTracking", activityRunning);

        if (activityRunning) {
            editor.putInt("numHours", Hours);
            editor.putInt("numSeconds", Seconds);
            editor.putInt("numMinutes", Minutes);
        }
        editor.apply();
    }

    @Override
    public void onStop() {
        Log.d("STOP", "onStop");
        ref.child("users").child(uid).child("currently_tracking").setValue(false);
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        Log.d("STOP", "onDestroyView");
        ref.child("users").child(uid).child("currently_tracking").setValue(false);
        super.onDestroyView();
    }

    public void updateMapPlotter(){
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();
        DatabaseReference dfRef =  FirebaseDatabase.getInstance().getReference("users").child(user.getUid()).child("device").child("current");
        dfRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot coords : dataSnapshot.getChildren()) {

                    double theLat = Double.parseDouble(coords.child("lat").getValue().toString());
                    double theLon = Double.parseDouble(coords.child("lon").getValue().toString());

                    if (preLat != null) {
                        preLat.add(theLat);
                        preLon.add(theLon);
                    }

                    mapPlotter.addMarker(theLat, theLon);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }
}