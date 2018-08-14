package com.watshout.watshout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.SnapshotReadyCallback;
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
import com.watshout.watshout.pojo.FriendRequestResponse;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import mad.location.manager.lib.Commons.Utils;
import mad.location.manager.lib.Services.KalmanLocationService;
import mad.location.manager.lib.Services.ServicesHelper;
import retrofit2.Call;
import retrofit2.Callback;

public class MapFragment extends android.app.Fragment implements OnMapReadyCallback, SensorEventListener,
        mad.location.manager.lib.Interfaces.LocationServiceInterface, mad.location.manager.lib.Interfaces.SimpleTempCallback {
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    LocationCallback locationCallback;

    FusedLocation fusedLocation;

    SensorManager sensorManager;

    ArrayList<String> requestIDs = new ArrayList<>();

    ArrayList<Marker> markerList;
    MapPlotter mapPlotter;
    MapView mv;

    ArrayList<LatLng> latLngList;

    RetrofitInterface retrofitInterface = RetrofitClient
            .getRetrofitInstance().create(RetrofitInterface.class);

    int totalSteps;

    PopupWindow popUp;

    //LayoutParams params;

    // General database reference
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

    Bitmap pathScreen;

    XMLCreator XMLCreator;

    PopupWindow popupWindow;
    LayoutInflater layoutInflater;

    RelativeLayout mRelativeLayout;

    // Resource file declarations
    Button mStart;
    Button mStop;

    Button mCamera;

    Button popUpStart;
    Button popUpStop;

    TextView timerText;
    TextView speedTextDialog;
    TextView stepsDialog;
    TextView distanceDialog;

    Boolean hasStrava;

    ImageButton mCenter;

    long originalStartTime;

    FloatingActionButton floatingActionButton;

    long MillisecondTime, StartTime, TimeBuff, UpdateTime = 0L ;
    Handler handler;
    int Seconds, Minutes, MilliSeconds ;

    private boolean timeRunning = false;

    String[] permissions = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION};

    // For permissions
    int permCode = 200;

    private final static int CAMERA = 350;

    public MapFragment() {
    }

    // This runs as the map rendering is completed
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {

        // IMPORTANT
        // This ensures that we can make changes to the map outside of this function
        // which is why we defined it globally
        googleMapGlobal = googleMap;

        try {
            googleMapGlobal.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.google_map_color
                    ));
        } catch (NullPointerException e){
            e.printStackTrace();
        }

        SharedPreferences settings = PreferenceManager
                .getDefaultSharedPreferences(getActivity());
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

        try {
            XMLCreator = new XMLCreator(getActivity().getApplicationContext(), uid);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }

        FriendDataManager friendDataManager = new FriendDataManager(uid, googleMapGlobal, mRecyclerView, getActivity(),
                new MapRecycleViewCarrier(mRecyclerView));

        // Starts location-getting process
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity().getApplicationContext());
        fusedLocation = null;
        try {
            fusedLocation = new FusedLocation(getActivity().getApplicationContext(),
                    mapPlotter, uid, XMLCreator, speedTextDialog, stepsDialog, distanceDialog);
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        locationRequest = fusedLocation.buildLocationRequest();
        locationCallback = fusedLocation.buildLocationCallback();
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
        System.out.println("Speed is: "  +fusedLocation.getTheSpeed());
        //distanceText.setText(fusedLocation.getTheSpeed() + "");
        mapPlotter.moveCamera(zoom);
    }


    /*public void setSpeed(Double speed) {
        distanceText.setText(speed + "");
    }*/

    public void onPause(){
        super.onPause();

        Log.d("PAUSE", "You just paused");

    }

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_map, container, false);
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        Log.d("MAP", "hello");
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("map");

        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        ref.child("users").child(uid).child("strava_token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                hasStrava = dataSnapshot.exists();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        mRelativeLayout = view.findViewById(R.id.relative);

        ServicesHelper.getLocationService(getActivity(), value -> {

            if (value.IsRunning()){
                return;
            }
            value.stop();
            KalmanLocationService.Settings settings =
                    new KalmanLocationService.Settings(Utils.ACCELEROMETER_DEFAULT_DEVIATION,
                            Utils.GPS_MIN_DISTANCE,
                            Utils.GPS_MIN_TIME,
                            Utils.GEOHASH_DEFAULT_PREC,
                            Utils.GEOHASH_DEFAULT_MIN_POINT_COUNT,
                            Utils.SENSOR_DEFAULT_FREQ_HZ,
                            null, false);

            value.reset(settings);
            value.start();

            Log.d("MAD", Boolean.toString(value.IsRunning()));
        });




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

        //final View dialogView = inflater.inflate(R.layout.fragment_dialog, null);
        final View popUpView = inflater.inflate(R.layout.fragment_dialog, null);
        builder.setView(popUpView);
        popUp = new PopupWindow(popUpView, displayWidth, displayHeight, true);

        mCamera = popUpView.findViewById(R.id.cameraButton);
        mCamera.setBackgroundResource(R.drawable.camera);
        mCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                //startActivityForResult(cameraIntent, CAMERA);

                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, CAMERA);


            }
        });

        FloatingActionButton fabDialog = (FloatingActionButton) popUpView.findViewById(R.id.fabDialog);
        fabDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.fui_slide_out_left);
                final ViewGroup hiddenPanel = (ViewGroup)popUpView.findViewById(R.id.dialogLayout);

                hiddenPanel.startAnimation(bottomUp);
                bottomUp.setAnimationListener(new Animation.AnimationListener() {
                    public void onAnimationStart(Animation anim)
                    {

                    };
                    public void onAnimationRepeat(Animation anim)
                    {
                    };
                    public void onAnimationEnd(Animation anim)
                    {
                        hiddenPanel.setVisibility(View.INVISIBLE);
                        popUp.dismiss();
                    };
                });

            }
        });

        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.fui_slide_in_right);
                ViewGroup hiddenPanel = (ViewGroup)popUpView.findViewById(R.id.dialogLayout);
                hiddenPanel.startAnimation(bottomUp);
                hiddenPanel.setVisibility(View.VISIBLE);
                popUp.showAtLocation(mRelativeLayout, Gravity.NO_GRAVITY, 0, 0);

            }
        });

        floatingActionButton.hide();

        checkLocationPermissions();

        // This helps the app not crash in certain contexts
        MapsInitializer.initialize(getActivity().getApplicationContext());

        mStart = view.findViewById(R.id.start);
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

        handler = new Handler() ;

        mStart.setBackgroundResource(R.drawable.round_button);

        isMapMoving = true;
        mv = view.findViewById(R.id.map);
        mv.onCreate(null);
        mv.onResume();
        mv.getMapAsync(this);

        mCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                double[] coords = fusedLocation.getLatestCoords();

                double latitude = coords[0];
                double longitude = coords[1];

                LatLng current = new LatLng(latitude, longitude);

                googleMapGlobal.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 19));

                googleMapGlobal.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.google_map_color
                        ));

            }
        });

        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                fusedLocation.resetLatLng();

                sendActivityNotification();
                //ActionBar actionBar = getSupportActionBar();
                ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                //actionBar.hide();
                actionBar.setDisplayHomeAsUpEnabled(false);
                actionBar.setHomeButtonEnabled(false);
                Animation bottomUp = AnimationUtils.loadAnimation(getActivity(),
                        R.anim.fui_slide_in_right);
                ViewGroup hiddenPanel = (ViewGroup)popUpView.findViewById(R.id.dialogLayout);
                hiddenPanel.startAnimation(bottomUp);
                hiddenPanel.setVisibility(View.VISIBLE);
                popUp.showAtLocation(mRelativeLayout, Gravity.NO_GRAVITY, 0, 0);
                startClick();


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

            timerText.setText("" + Minutes + ":"
                    + String.format("%02d", Seconds));

            handler.postDelayed(this, 0);

            if(!currentlyTrackingLocation){
                //captureMapScreen();
            }


            if (counter % 20000 == 0) {
                //captureMapScreen();
            }

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

                    // Permission enabled

                } else {

                    checkLocationPermissions();

                }
            }
        }
    }

    public void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this.getActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this.getActivity(),
                    permissions,
                    permCode);

        }
    }
    @Override
    public void onSensorChanged(SensorEvent event) {
        System.out.println("value of event is: " + event.values[0] + " "
                );

       if(currentlyTrackingLocation) {

           System.out.println("Total Steps: " + totalSteps);
           stepsDialog.setText(String.valueOf((int) (event.values[0])) + " steps");

        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}


    @Override
    public void onResume() {
        super.onResume();
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);

    }

    public void captureMapScreen()
    {
        SnapshotReadyCallback callback = new SnapshotReadyCallback() {

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // TODO Auto-generated method stub
                pathScreen = snapshot;

            }
        };

        googleMapGlobal.snapshot(callback);
    }

    public boolean allWhite(Bitmap bmp){
        for (int i=0;i<bmp.getWidth();i++){
            for (int j=0;j<bmp.getHeight();j++){
                int clr=  bmp.getPixel(i,j);
                int  red   = (clr & 0x00ff0000) >> 16;
                int  green = (clr & 0x0000ff00) >> 8;
                int  blue  =  clr & 0x000000ff;
                if (!(red == 255 && green==255 && blue==255))
                    return false;
            }
        }
        return true;
    }

    public void startClick() {

        floatingActionButton.show();
        mStart.setVisibility(View.INVISIBLE);
        mStop.setVisibility(View.INVISIBLE);
        mStart.setText("");
        popUpStart.setText("");
        popUpStop.setVisibility(View.VISIBLE);

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
                public void onCancelled(DatabaseError databaseError) { }
            });

            activityRunning = true;
        }

        currentlyTrackingLocation = !currentlyTrackingLocation;

        if (currentlyTrackingLocation){
            mStart.setBackgroundResource(R.drawable.pause);
            mStart.setText("");
            //mStart.setText("Pause");

            popUpStart.setBackgroundResource(R.drawable.pause);
            //popUpStart.setText("Pause");
            mStart.setText("");
        } else {
            mStart.setBackgroundResource(R.drawable.resume);
            //mStart.setText("Resume");
            mStart.setText("");

            popUpStart.setBackgroundResource(R.drawable.resume);
            //popUpStart.setText("Resume");
            mStart.setText("");
        }


    }

    public void stopClick() {

        floatingActionButton.show();

        currentlyTrackingLocation = false;
        activityRunning = false;

        String encodedPath = PolyUtil.encode(fusedLocation.getLatLng());
        String mapURL = EndpointURL.getInstance().getCreateMapURL() + encodedPath;

        Intent openNext = new Intent(getActivity().getApplicationContext(), FinishedActivity.class);

        // Generates a current date
        UploadToDatabase uploadToDatabase = new UploadToDatabase();
        String date = uploadToDatabase.getFormattedDate();

        // send bitmap as byte array
        //ByteArrayOutputStream stream = new ByteArrayOutputStream();
        //pathScreen.compress(Bitmap.CompressFormat.PNG, 100, stream);
        //byte[] byteArray = stream.toByteArray();
        //pathScreen.recycle();

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.running_black);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] bitmapdata = stream.toByteArray();

        openNext.putExtra("MAP_IMAGE", bitmapdata);
        openNext.putExtra("MAP_URL", mapURL);
        openNext.putExtra("STRAVA", Boolean.toString(hasStrava));
        openNext.putExtra("GPX_NAME_ONLY", date);
        openNext.putExtra("GPX_NAME",date+".gpx");
        openNext.putExtra("MIN", Minutes);
        openNext.putExtra("SEC", Seconds);

        // Writes an XML file
        try {
            XMLCreator.saveFile(date);
        } catch (TransformerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Carrier.setXMLCreator(XMLCreator);

        openNext.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getActivity().getApplicationContext().startActivity(openNext);
        getActivity().finish();
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
    public void locationChanged(Location location) {
        Log.d("MAD", "This is running");
        Log.d("MAD", location.getLatitude() + "");
        Toast.makeText(getActivity(), "Lat: " + location.getLatitude(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCall(Object o) {

    }
}