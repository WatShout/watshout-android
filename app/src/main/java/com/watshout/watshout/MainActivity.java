package com.watshout.watshout;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.Image;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.samsandberg.stravaauthenticator.StravaAuthenticateActivity;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;


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

    static DrawerLayout mDrawerLayout;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String email = thisUser.getEmail();
    String uid = thisUser.getUid();
    String name = thisUser.getDisplayName();

    NavigationView navigationView;

    String CURRENT_DEVICE_ID;
    String stravaToken;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private StorageReference mStorageRef;
    private DatabaseReference activityImagesRef;

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    LatLng home;

    // Gets a unique hardware ID for a device
    @SuppressLint("HardwareIds")
    String getDeviceID() {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        CURRENT_DEVICE_ID = getDeviceID();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger);

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        disableNavigationViewScrollbars(navigationView);

        if (!thisUser.isEmailVerified()){

            thisUser.sendEmailVerification();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setTitle("Email verification sent");
            builder.setMessage("We ask that all new users please verify their " +
                    "email address. Please follow the link sent to your email " +
                    "and then you will be able to log in.");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    mAuth.signOut();
                    Intent reopenSignIn = new Intent(getApplicationContext(), SignInActivity.class);
                    getApplicationContext().startActivity(reopenSignIn);

                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();

        } else {
            // Checks if a user has an account entry. If not they get redirected
            ref.child("users").child(uid).child("profile_pic_format").addListenerForSingleValueEvent(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (!dataSnapshot.exists()){
                                Intent initialize = new Intent(getApplicationContext(), InitializeNewAccountActivity.class);
                                finish();
                                startActivity(initialize);
                            }

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) { }
                    }
            );
        }


        ref.child("users").child(uid).child("device").child("ID").setValue(CURRENT_DEVICE_ID);
        ref.child("users").child(uid).child("device").child("name").setValue(android.os.Build.MODEL);

        if (!isNetworkAvailable()){
            Toast.makeText(this, "No network detected. App may not work as intended",
                    Toast.LENGTH_LONG).show();
        }

        // This code only runs if the user just authenticated with Strava in settings
        stravaToken = getIntent().getStringExtra("STRAVA_TOKEN");
        if (stravaToken != null){
            ref.child("users").child(uid).child("strava_token").setValue(stravaToken);
            Toast.makeText(this, "Successfully connected with Strava!",
                    Toast.LENGTH_SHORT).show();
        }

        try {
            ref.child("users").child(uid).child("fcm_token").setValue(FirebaseInstanceId.getInstance().getToken());
        } catch (NullPointerException e){
            e.printStackTrace();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String token = prefs.getString("fcm_token", null);

            if (token != null){

                ref.child("users").child(uid).child("fcm_token").setValue(token);

            }
        }

        Menu m = navigationView.getMenu();
        MenuItem menuItem = m.findItem(R.id.nav_activity);
        SpannableString spannableString = new SpannableString(menuItem.getTitle());
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ffd529")),
                0, spannableString.length(), 0);
        menuItem.setTitle(spannableString);

        View headerView = navigationView.getHeaderView(0);

        navigationView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // This sets the user's initials but it looks like we're just using profile pics now
        /*
        TextView mInitials = headerView.findViewById(R.id.nav_header_initials);
        String initials = "";
        for (String s : name.split(" ")) {
            initials += s.charAt(0);
        }
        mInitials.setText(initials);
        */

        TextView mName = headerView.findViewById(R.id.nav_header_name);
        mName.setText(name);

        TextView mEmail = headerView.findViewById(R.id.nav_header_email);
        mEmail.setText(email);

        ImageView mCircleProfilePic = headerView.findViewById(R.id.profilePic);
        ref.child("users").child(uid).child("profile_pic_format")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        try {
                            String extension = (String) dataSnapshot.getValue();

                            storageReference.child("users").child(uid)
                                    .child("profile." + extension)
                                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Picasso.get()
                                            .load(uri)
                                            .resize(64, 64)
                                            .transform(new CircleTransform())
                                            .placeholder(R.drawable.loading)
                                            .into(mCircleProfilePic);

                                }
                            });

                        } catch (NullPointerException e){

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        final TextView mLastActive = headerView.findViewById(R.id.nav_header_last_active);
        ref.child("users").child(uid).child("device").child("past").orderByChild("time").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        mLastActive.setText("Never");

                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            try {
                                String date;
                                Long time = ds.child("time").getValue(Long.class);

                                try {
                                    date = new java.text.SimpleDateFormat("MMM dd")
                                            .format(new java.util.Date (time));

                                    Log.d("DATE", date);

                                } catch (NullPointerException e){
                                    date = "Never";
                                }

                                mLastActive.setText(date);
                            } catch (DatabaseException e){
                                mLastActive.setText("Never");
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        final TextView mRuns = headerView.findViewById(R.id.nav_header_total_runs);
        ref.child("users").child(uid).child("device").child("past").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String runCount = dataSnapshot.getChildrenCount() + "";
                mRuns.setText(runCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final TextView mFriends = headerView.findViewById(R.id.nav_header_friends);
        ref.child("friend_data").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String friendCount = dataSnapshot.getChildrenCount() + "";
                mFriends.setText(friendCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        final TextView mDistance = headerView.findViewById(R.id.nav_header_total_distance);
        mDistance.setText("N/A");
        final double KM_TO_MILE = 0.621371;

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        final String units = settings.getString("Units", "Imperial");

        ref.child("users").child(uid).child("device").child("past").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                double total = 0;

                try {
                    for (DataSnapshot snapshotChild : dataSnapshot.getChildren()) {
                        double distance = Double.valueOf(snapshotChild.child("distance").getValue(String.class));
                        total += distance;
                    }

                    if (units.equals("Imperial")){
                        total = total * KM_TO_MILE;
                        mDistance.setText(total + " mi");
                    } else {
                        mDistance.setText(total + " km");
                    }
                } catch (NullPointerException e){
                    mDistance.setText("N/A");
                } catch (DatabaseException e){
                    mDistance.setText("N/A");
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Ideally we would want this to be the location one is at when they start the app
        home = new LatLng(37.4419, -122.1430);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.screen_area, new MapFragment())
                .commit();

        ref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // generate color for new user
                // 5 colors -> red, yellow, green, blue, magenta
                String[] colorNames = {"red", "yellow", "green", "blue", "magenta"};

                if(!dataSnapshot.hasChild("color"))
                {
                    int selection = (int) (Math.random() * colorNames.length);
                    ref.child("users").child(uid).child("color").push();
                    ref.child("users").child(uid).child("color").setValue(colorNames[selection]);
                    Log.i("GenColor","Generated color successfully.");
                } else Log.i("GenColor","Loaded color successfully.");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        // set item as selected to persist highlight
        int id = menuItem.getItemId();

        switch (id) {

            case R.id.nav_news_feed:
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.screen_area, new NewsFeedFragment())
                        .commit();
                break;


            case R.id.nav_activity:
                MapFragment activityFragment = new MapFragment();
                Bundle activityBundle = new Bundle();
                activityBundle.putString("type", "activity");
                activityFragment.setArguments(activityBundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.screen_area, activityFragment)
                        .commit();
                break;


            case R.id.nav_map:
                MapFragment mapFragment = new MapFragment();
                Bundle mapBundle = new Bundle();
                mapBundle.putString("type", "map");
                mapFragment.setArguments(mapBundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.screen_area, mapFragment)
                        .commit();

                break;

            case R.id.nav_calendar:
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.screen_area, new CalendarFragment())
                        .commit();
                break;

            case R.id.nav_settings:
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.screen_area, new SettingsFragment())
                        .commit();
                break;

            case R.id.nav_friends:
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.screen_area, new FriendFragment())
                        .commit();
                break;

            case R.id.nav_signout:
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.screen_area, new SignOutFragment())
                        .commit();
                break;

        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.image_toolbar, menu);
        return true;
    }

    private void disableNavigationViewScrollbars(NavigationView navigationView) {
        if (navigationView != null) {
            NavigationMenuView navigationMenuView = (NavigationMenuView) navigationView.getChildAt(0);
            if (navigationMenuView != null) {
                navigationMenuView.setVerticalScrollBarEnabled(false);
            }
        }
    }

    @Override
    public void onBackPressed(){

        navigationView.setCheckedItem(R.id.nav_map);

        // set to MapFragment
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.screen_area, new MapFragment())
                .commit();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static DrawerLayout getDrawerLayout(){
        return mDrawerLayout;
    }
}
