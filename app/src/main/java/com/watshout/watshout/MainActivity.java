package com.watshout.watshout;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

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

    DrawerLayout mDrawerLayout;
    MapFragment mapFragment = new MapFragment();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String email = thisUser.getEmail();
    String uid = thisUser.getUid();
    String name = thisUser.getDisplayName();

    String CURRENT_DEVICE_ID;

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

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //navigationView.setItemIconTintList(null);
        disableNavigationViewScrollbars(navigationView);

        Menu m = navigationView.getMenu();
        MenuItem menuItem = m.findItem(R.id.nav_activity);
        SpannableString spannableString = new SpannableString(menuItem.getTitle());
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ffd529")),
                0, spannableString.length(), 0);
        menuItem.setTitle(spannableString);

        View headerView = navigationView.getHeaderView(0);

        navigationView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        TextView mInitials = headerView.findViewById(R.id.nav_header_initials);
        String initials = "";
        for (String s : name.split(" ")) {
            initials += s.charAt(0);
        }
        mInitials.setText(initials);

        TextView mName = headerView.findViewById(R.id.nav_header_name);
        mName.setText(name);

        TextView mEmail = headerView.findViewById(R.id.nav_header_email);
        mEmail.setText(email);

        final TextView mLastActive = headerView.findViewById(R.id.nav_header_last_active);
        ref.child("users").child(uid).child("device").child("past").orderByChild("time").limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            Long time = ds.child("time").getValue(Long.class);

                            String date;

                            try {
                                date = new java.text.SimpleDateFormat("MMM dd")
                                        .format(new java.util.Date (time));

                            } catch (NullPointerException e){
                                date = "Never";
                            }

                            mLastActive.setText(date);

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

        TextView mDistance = headerView.findViewById(R.id.nav_header_total_distance);
        mDistance.setText("N/A");


        // Ideally we would want this to be the location one is at when they start the app
        home = new LatLng(37.4419, -122.1430);

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.screen_area, mapFragment)
                .commit();

        ref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // If data snapshot doesn't exist
                if (!dataSnapshot.exists()) {

                    // Open activity

                    Intent openPfp = new Intent(getApplicationContext(), InitializeNewAccountActivity.class);
                    openPfp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(openPfp);

                } else {

                    // Just set values

                    ref.child("users").child(uid).child("device").child("ID").setValue(CURRENT_DEVICE_ID);
                    ref.child("users").child(uid).child("device").child("name").setValue(android.os.Build.MODEL);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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

        } else if (id == R.id.nav_friends) {

            getFragmentManager()
                    .beginTransaction()
                    .replace(R.id.screen_area, new FriendFragment())
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
        Toast.makeText(this,"Returned to map.",Toast.LENGTH_LONG).show();

        // set to MapFragment
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.screen_area, new MapFragment())
                .commit();
    }
}
