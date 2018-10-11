package com.watshout.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.internal.NavigationMenuView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import com.squareup.picasso.Picasso;
import com.watshout.app.pojo.FriendRequest;
import com.watshout.app.pojo.FriendRequestList;

import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;


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

    Menu globalMenu;
    MenuInflater globalInflater;

    NavigationView navigationView;

    String CURRENT_DEVICE_ID;

    SharedPreferences prefs;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    RetrofitInterface retrofitInterface = RetrofitClient
            .getRetrofitInstance().create(RetrofitInterface.class);

    // Gets a unique hardware ID for a device
    @SuppressLint("HardwareIds")
    String getDeviceID() {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(),
                Settings.Secure.ANDROID_ID);
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = new Intent();
        String packageName = this.getPackageName();
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        Log.d("BATTERY_INFOO", Boolean.toString(pm.isIgnoringBatteryOptimizations(packageName)));
        if (pm.isIgnoringBatteryOptimizations(packageName)) {
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
            Log.d("BATTERY_INFO", "We are ignoring battery optimization");
        }
        else {
            Log.d("BATTERY_INFO", "We are NOT ignoring battery optimization");
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
            this.startActivity(intent);

        }

        runInitialChecks();
        updateDeviceInfo();
        checkHasNetwork();
        setFCMToken();
        displayInitialSplash();

        setBatteryOptimizationSplash();

        // Initialize navigation view
        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        disableNavigationViewScrollbars(navigationView);
        Menu m = navigationView.getMenu();
        MenuItem menuItem = m.findItem(R.id.nav_activity);
        SpannableString spannableString = new SpannableString(menuItem.getTitle());
        spannableString.setSpan(new ForegroundColorSpan(Color.parseColor("#ffd529")),
                0, spannableString.length(), 0);
        menuItem.setTitle(spannableString);
        View headerView = navigationView.getHeaderView(0);
        navigationView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        navigationView.setCheckedItem(R.id.nav_activity);

        // Set up action bar with hamburger button
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger);

        // Initialize Header UI elements
        TextView mName = headerView.findViewById(R.id.nav_header_name);
        TextView mEmail = headerView.findViewById(R.id.nav_header_email);
        ImageView mCircleProfilePic = headerView.findViewById(R.id.profilePic);
        TextView mLastActive = headerView.findViewById(R.id.nav_header_last_active);
        TextView mRuns = headerView.findViewById(R.id.nav_header_total_runs);
        TextView mFriends = headerView.findViewById(R.id.nav_header_friends);
        TextView mDistance = headerView.findViewById(R.id.nav_header_total_distance);

        mName.setText(name);
        mEmail.setText(email);
        // Placeholder until DB call
        mDistance.setText("N/A");

        // TODO
        ref.child("friend_requests").child(uid).orderByChild("request_type")
                .equalTo("received")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        //globalMenu.clear();
                        //globalInflater.inflate(R.menu.base_menu_requests, globalMenu);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        //globalMenu.clear();
                        //globalInflater.inflate(R.menu.base_menu_no_requests, globalMenu);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

                    @Override
                    public void onCancelled(DatabaseError databaseError) { }
                });

        // Download and update the profile picture
        ref.child("users").child(uid).child("profile_pic_format")
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (dataSnapshot.exists()) {
                        String extension = (String) dataSnapshot.getValue();

                        storageReference.child("users").child(uid)
                                .child("profile." + extension)
                                .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Picasso.get()
                                        .load(uri)
                                        .resize(128, 128)
                                        .transform(new CircleTransform())
                                        .placeholder(R.drawable.large_no_word)
                                        .into(mCircleProfilePic);
                            }
                        });
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });

        // Set lastActive value
        ref.child("users").child(uid).child("device").child("past").orderByChild("time").limitToLast(1)
            .addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    mLastActive.setText("Never");

                    for(DataSnapshot ds : dataSnapshot.getChildren()) {

                        String date;
                        Long time = ds.child("time").getValue(Long.class);

                        try {
                            date = new java.text.SimpleDateFormat("MMM dd")
                                    .format(new java.util.Date (time));

                        } catch (NullPointerException e) {
                            date = "Never";
                        }

                        mLastActive.setText(date);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });

        // Set the number of runs completed
        ref.child("users").child(uid).child("device").child("past")
                .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String runCount = dataSnapshot.getChildrenCount() + "";
                mRuns.setText(runCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        // Set the number of friends
        ref.child("friend_data").child(uid)
            .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String friendCount = dataSnapshot.getChildrenCount() + "";
                mFriends.setText(friendCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        ref.child("users").child(uid).child("device").child("past")
            .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                double total = 0;

                if (dataSnapshot.exists()) {

                    for (DataSnapshot snapshotChild : dataSnapshot.getChildren()) {
                        double distance = snapshotChild.child("distance").getValue(Double.class);
                        total += distance;
                    }

                    DecimalFormat decimalFormat = new DecimalFormat("##.##");
                    String totalDistance = decimalFormat.format(total);

                    mDistance.setText(totalDistance);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        MapFragment activityFragment = new MapFragment();

        getFragmentManager()
                .beginTransaction()
                .replace(R.id.screen_area, activityFragment)
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

            case R.id.nav_feedback:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.watshout.com/feedback"));
                startActivity(browserIntent);
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
                return super.onOptionsItemSelected(item);
            case R.id.icon:
                navigationView.getMenu().getItem(0).setChecked(true);
                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.screen_area, new MapFragment())
                        .commit();
                return super.onOptionsItemSelected(item);
            case R.id.open_requests:

                LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_friend_request, null);
                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it

                PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.setAnimationStyle(R.style.popup_window_animation);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                TextView goBack = popupView.findViewById(R.id.otherGoBack);

                goBack.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupWindow.dismiss();
                    }
                });

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {

                        globalMenu.clear();
                        globalInflater.inflate(R.menu.base_menu_no_requests, globalMenu);

                    }
                });

                RelativeLayout relativeLayout = popupView.findViewById(R.id.request_relative_layout);

                relativeLayout.setAlpha(1F);

                RecyclerView mRequestRecyclerView = popupView.findViewById(R.id.friendRequestView);
                mRequestRecyclerView.setHasFixedSize(true);
                mRequestRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                getFriendRequests(mRequestRecyclerView);

                return super.onOptionsItemSelected(item);

            default:
                return super.onOptionsItemSelected(item);
        }
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        globalInflater = inflater;
        globalMenu = menu;
        inflater.inflate(R.menu.base_menu_no_requests, menu);
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

    public void getFriendRequests(final RecyclerView popUpRecyclerView){

        Call<FriendRequestList> call = retrofitInterface.getFriendRequestList(uid);

        call.enqueue(new Callback<FriendRequestList>() {
            @Override
            public void onResponse(Call<FriendRequestList> call, retrofit2.Response<FriendRequestList> response) {

                List<FriendRequest> friendRequestList = response.body().getFriendRequests();
                RecyclerView.Adapter adapter =
                        new FriendRequestAdapter(friendRequestList, MainActivity.this);

                popUpRecyclerView.setAdapter(adapter);

            }

            @Override
            public void onFailure(Call<FriendRequestList> call, Throwable t) {

            }
        });
    }

    @Override
    public void onBackPressed(){

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.START);

        if (drawerOpen) {
            mDrawerLayout.closeDrawer(Gravity.LEFT);
        } else {
            mDrawerLayout.openDrawer(GravityCompat.START);
        }

      /*
        navigationView.setCheckedItem(R.id.nav_activity);

        // set to MapFragment
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.screen_area, new MapFragment())
                .commit();
                */
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

    public void updateDeviceInfo() {
        CURRENT_DEVICE_ID = getDeviceID();
        ref.child("users").child(uid).child("device").child("ID").setValue(CURRENT_DEVICE_ID);
        ref.child("users").child(uid).child("device").child("name").setValue(android.os.Build.MODEL);
    }

    public void runInitialChecks() {

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
    }

    public void checkHasNetwork() {
        if (!isNetworkAvailable()){
            Toast.makeText(this, "No network detected. App may not work as intended",
                    Toast.LENGTH_LONG).show();
        }
    }

    public void setFCMToken() {
        try {
            ref.child("users").child(uid).child("fcm_token")
                    .setValue(FirebaseInstanceId.getInstance().getToken());
        } catch (NullPointerException e){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String token = prefs.getString("fcm_token", null);

            if (token != null){
                ref.child("users").child(uid).child("fcm_token").setValue(token);
            }
        }
    }

    public void displayInitialSplash() {

        SharedPreferences thisPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean initialRun = thisPrefs.getBoolean("initialRun", true);

        if (initialRun) {

            thisPrefs.edit().putBoolean("initialRun", false).apply();

            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Thank you for trying Watshout!")
                    .setMessage("For optimal results during activity tracking, we recommend " +
                    "keeping Watshout running in the foreground. Potential bugs may arise if " +
                    "the app is closed in the middle of an activity. For the smoothest Watshout " +
                            "experience, we strongly recommend you whitelist our app from Android's " +
                            "standby mode. To do this, go to Settings > Battery " +
                    "> Battery Optimization > All apps, find Watshout and select \"Don't optimize\".")
                    .setPositiveButton("UNDERSTOOD", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) { }
                    });
            builder.create();
            builder.show();
        }
    }

    public void setBatteryOptimizationSplash() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        SharedPreferences thisPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        boolean initialRun = thisPrefs.getBoolean("initialRun", true);

        if (initialRun) {
            builder.setTitle("Disable Battery Optimization");
            builder.setMessage("For the smoothest Watshout experience, we strongly recommend you " +
                    "whitelist our app from Android's standby mode. To do this, go to Settings > Battery " +
                    "> Battery Optimization > All apps, find Watshout and select \"Don't optimize\"");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) { }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
}
