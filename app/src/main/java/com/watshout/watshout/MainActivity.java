package com.watshout.watshout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
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
import com.watshout.watshout.pojo.Friend;
import com.watshout.watshout.pojo.FriendRequest;
import com.watshout.watshout.pojo.FriendRequestList;
import com.watshout.watshout.pojo.FriendRequestResponse;
import com.watshout.watshout.pojo.FriendsList;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
    String stravaToken;

    public LinearLayout mDrawerLinearLayout;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private StorageReference mStorageRef;
    private DatabaseReference activityImagesRef;

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    RetrofitInterface retrofitInterface = RetrofitClient
            .getRetrofitInstance().create(RetrofitInterface.class);

    LatLng home;
    final boolean[] hasRequests = {false};

    // Gets a unique hardware ID for a device
    @SuppressLint("HardwareIds")
    String getDeviceID() {
        return Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    @Override
    public void onResume(){
        super.onResume();
        Log.d("STOP", "onResume");

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        hasRequests[0] = false;

        CURRENT_DEVICE_ID = getDeviceID();

        mDrawerLayout = findViewById(R.id.drawer_layout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //mDrawerLinearLayout = (LinearLayout) findViewById(R.id.drawer_layout);

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
        navigationView.setCheckedItem(R.id.nav_activity);

        // This sets the user's initials but it looks like we're just using profile pics now
        /*
        TextView mInitials = headerView.findViewById(R.id.nav_header_initials);
        String initials = "";
        for (String s : name.split(" ")) {
            initials += s.charAt(0);
        }
        mInitials.setText(initials);
        */

        ref.child("friend_requests").child(uid).orderByChild("request_type")
                .equalTo("received")
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        globalMenu.clear();
                        globalInflater.inflate(R.menu.friend_menu_requests, globalMenu);
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {
                        globalMenu.clear();
                        globalInflater.inflate(R.menu.friend_menu_no_requests, globalMenu);
                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


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
                                            .resize(128, 128)
                                            .transform(new CircleTransform())
                                            .placeholder(R.drawable.large_no_word)
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

        ref.child("users").child(uid).child("device").child("past").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                double total = 0;

                try {
                    for (DataSnapshot snapshotChild : dataSnapshot.getChildren()) {
                        double distance = Double.valueOf(snapshotChild.child("distance").getValue(String.class));
                        total += distance;
                    }

                    DecimalFormat decimalFormat = new DecimalFormat("##.##");
                    String totalDistance = decimalFormat.format(total);

                    mDistance.setText(totalDistance);

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

        MapFragment activityFragment = new MapFragment();
        Bundle activityBundle = new Bundle();
        activityBundle.putString("type", "activity");
        activityFragment.setArguments(activityBundle);

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

            /*
            case R.id.nav_map:
                MapFragment mapFragment = new MapFragment();
                Bundle mapBundle = new Bundle();
                mapBundle.putString("type", "activity");
                mapFragment.setArguments(mapBundle);

                getFragmentManager()
                        .beginTransaction()
                        .replace(R.id.screen_area, mapFragment)
                        .commit();

                break;*/

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

                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {

                        globalMenu.clear();
                        globalInflater.inflate(R.menu.friend_menu_no_requests, globalMenu);

                    }
                });

                RelativeLayout relativeLayout = popupView.findViewById(R.id.request_relative_layout);

                relativeLayout.setAlpha(1F);

                RecyclerView mRequestRecyclerView = popupView.findViewById(R.id.friendRequestView);
                mRequestRecyclerView.setHasFixedSize(true);
                mRequestRecyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                getFriendRequests(mRequestRecyclerView);

                return super.onOptionsItemSelected(item);
            case R.id.send_request:
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
                android.app.AlertDialog dialog;
                builder.setTitle("Search Friend by Email");

                // Set up the input
                final EditText input = new EditText(this);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Spinning dialog when adding friend
                        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
                        progressDialog.setMessage("Adding friend...");

                        sendRequest(input.getText().toString(), progressDialog);

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                dialog = builder.create();
                dialog.getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                builder.show();
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
        inflater.inflate(R.menu.friend_menu_no_requests, menu);
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

    public void firebaseFriendRequest(String theirUID){
        ref.child("friend_requests").child(uid).child(theirUID)
                .child("request_type").setValue("sent");

        ref.child("friend_requests").child(theirUID).child(uid)
                .child("request_type").setValue("received");
    }

    public void sendRequest(String theirEmail, final ProgressDialog progressDialog) {

        progressDialog.show();

        ref.child("users").orderByChild("email").equalTo(theirEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null){

                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

                                final String theirUID = childSnapshot.getKey();

                                ref.child("friend_data").child(uid).child(theirUID)
                                        .addListenerForSingleValueEvent(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {

                                                if (dataSnapshot.getValue() != null){

                                                    Toast.makeText(MainActivity.this,
                                                            "You are already friends with this user",
                                                            Toast.LENGTH_SHORT).show();

                                                } else {

                                                    firebaseFriendRequest(theirUID);

                                                    Call<FriendRequestResponse> call =
                                                            retrofitInterface.sendFriendNotification(uid, theirUID);

                                                    call.enqueue(new Callback<FriendRequestResponse>() {
                                                        @Override
                                                        public void onResponse(Call<FriendRequestResponse> call, retrofit2.Response<FriendRequestResponse> response) {

                                                        }

                                                        @Override
                                                        public void onFailure(Call<FriendRequestResponse> call, Throwable t) {

                                                        }
                                                    });


                                                }
                                                progressDialog.dismiss();


                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                            }



                        } else {

                            Toast.makeText(MainActivity.this, "User not found",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("EMAIL", databaseError.toString());
                        progressDialog.dismiss();
                    }
                });


    }
}
