package com.watshout.tracker;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;


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

    private StorageReference mStorageRef;
    private DatabaseReference activityImagesRef;

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();


    LatLng home;


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


        // Ideally we would want this to be the location one is at when they start the app
        home = new LatLng(37.4419, -122.1430);

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
