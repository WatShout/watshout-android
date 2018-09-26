package com.watshout.watshout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

public class FinishedActivity extends AppCompatActivity{

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String uid = thisUser.getUid();
    String date;

    boolean hasStrava;
    boolean wantsToUploadStrava;

    final double KM_TO_MILE = 0.621371;

    //CheckBox stravaCheckBox;
    ImageView returnToMap;
    TextView time;
    TextView distance;
    TextView pace;
    //String mapURL;
    String displayMapURL;
    String uploadMapURL;
    ImageView mFinishedRun;
    TextView mDistLabel;
    TextView mTimeLabel;
    TextView mPaceLabel;
    TextView mStatisticsText;
    ImageView mShrinker;

    RelativeLayout mBottomLayout;

    Button uploadGpx;

    boolean isExpanded = true;

    // General database reference
    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();


    void loadMapImage() {
        Picasso.get()
                .load(displayMapURL)
                .into(mFinishedRun);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);

        Window window = this.getWindow();

        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        // finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        //stravaCheckBox = findViewById(R.id.stravaBox);
        returnToMap = findViewById(R.id.back);
        time = findViewById(R.id.time);
        distance = findViewById(R.id.distance);
        pace = findViewById(R.id.pace);
        mFinishedRun = findViewById(R.id.finishedRun);
        mDistLabel = findViewById(R.id.distLabel);
        mPaceLabel = findViewById(R.id.paceLabel);
        mTimeLabel = findViewById(R.id.timeLabel);
        mShrinker = findViewById(R.id.shrinker);
        mStatisticsText = findViewById(R.id.statText);
        uploadGpx = findViewById(R.id.uploadGpx);
        mBottomLayout = findViewById(R.id.bottomLayout);

        final float scale = getResources().getDisplayMetrics().density;
        int pixels = (int) (250 * scale + 0.5f);

        RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, pixels);
        rel_btn.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        mBottomLayout.setLayoutParams(rel_btn);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        //String units = settings.getString("Units", "Metric");
        String units = "Imperial";

        // Get value to determine whether or nlt to show checkbox
        hasStrava = Boolean.valueOf(getIntent().getStringExtra("STRAVA"));
        displayMapURL = getIntent().getStringExtra("DISPLAY_MAP_URL");
        uploadMapURL = getIntent().getStringExtra("UPLOAD_MAP_URL");

        loadMapImage();

        if (!hasStrava) {
            //stravaCheckBox.setVisibility(View.INVISIBLE);
        }

        // load time and distance data
        final int min = getIntent().getIntExtra("MIN",0);
        final int sec = getIntent().getIntExtra("SEC",0);

        DecimalFormat formatter = new DecimalFormat("00");
        String formattedMin = formatter.format(min);
        String formattedSec = formatter.format(sec);

        time.setText(formattedMin + ":" + formattedSec);

        final double rawDistance = Double.valueOf(getIntent().getStringExtra("DISTANCE"));

        // Making this imperial for now
        //final PaceCalculator pc = new PaceCalculator(rawMetricDistance, min, sec, this);
        final PaceCalculator pc = new PaceCalculator(rawDistance, min, sec);

        distance.setText(pc.getDistance() + "");
        pace.setText(pc.getPace() + "");

        // load GPX from carrier class
        final XMLCreator XMLCreator = Carrier.getXMLCreator();

        // Get GPX file name from Intent
        date = getIntent().getStringExtra("GPX_NAME");
        date = date.substring(0, date.length() - 4);

        mShrinker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int visible = isExpanded ? View.INVISIBLE : View.VISIBLE;
                Drawable icon = isExpanded ? ResourcesCompat.getDrawable(getResources(),
                        R.drawable.black_arrow_up, null) :
                        ResourcesCompat.getDrawable(getResources(),
                                R.drawable.black_arrow_down, null);

                int height = isExpanded ? 50 : 250;

                final float scale = getResources().getDisplayMetrics().density;
                int pixels = (int) (height * scale + 0.5f);

                RelativeLayout.LayoutParams rel_btn = new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, pixels);
                rel_btn.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                mBottomLayout.setLayoutParams(rel_btn);

                mShrinker.setImageDrawable(icon);

                mDistLabel.setVisibility(visible);
                mPaceLabel.setVisibility(visible);
                mTimeLabel.setVisibility(visible);

                mStatisticsText.setVisibility(visible);

                uploadGpx.setVisibility(visible);

                distance.setVisibility(visible);
                time.setVisibility(visible);
                pace.setVisibility(visible);

                isExpanded = !isExpanded;

            }
        });

        uploadGpx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ProgressDialog progressDialog = new ProgressDialog(FinishedActivity.this);
                progressDialog.setMessage("Uploading run data...");
                progressDialog.show();

                // If user checked box, then upload to Strava
                //wantsToUploadStrava = stravaCheckBox.isChecked();

                UploadToDatabase uploadToDatabase = new UploadToDatabase(uid,
                        pc.getDistance() + "",
                        pc.getPace(),
                        pc.getTotalSeconds(),
                        uploadMapURL);

                uploadToDatabase.moveCurrentToPast(date);

                // Upload GPX to Firebase Storage
                //XMLCreator.uploadToFirebaseStorage(date, wantsToUploadStrava);
                //XMLCreator.resetXML();

                progressDialog.dismiss();

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FinishedActivity.this);
                SharedPreferences.Editor editor = preferences.edit();

                // Ethan
                editor.putBoolean("currentlyTracking", false);
                editor.apply();

                // Redirect to MapFragment
                Intent openMain = new Intent(getApplicationContext(), MainActivity.class);
                openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(openMain);
                finish();

            }
        });

        returnToMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){

                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FinishedActivity.this);
                SharedPreferences.Editor editor = preferences.edit();

                // Ethan
                editor.putBoolean("currentlyTracking", false);
                editor.apply();

                // Removes current from from 'current' entry
                UploadToDatabase uploadToDatabase = new UploadToDatabase(uid);
                uploadToDatabase.removeCurrentEntry();

                // Redirect to MapFragment
                Intent openMain = new Intent(getApplicationContext(), MainActivity.class);
                openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(openMain);
                finish();
            }
        });

    }

    // Returns distance in MILES
    public double findDistanceFromGpx(String fileName){

        File path = this.getExternalFilesDir(null);
        File file = new File(path, fileName);

        BufferedReader reader;

        final double COORD_TO_MILE = 69.172;
        final double MILE_TO_KM = 1.60934;
        double dist = 0;

        try{
            reader = new BufferedReader(new FileReader(file));

            double lon = 0;
            double lat = 0;
            while (true){
                String line = reader.readLine();
                if (line==null) break;

                if (line.contains("trkpt") && !line.contains("/trkpt")){
                    int firstQuote = line.indexOf('"');
                    int secondQuote = line.indexOf('"',firstQuote+1);
                    int thirdQuote = line.indexOf('"',secondQuote+1);
                    int fourthQuote = line.indexOf('"',thirdQuote+1);

                    double newLon = Double.parseDouble(line.substring(firstQuote+1,secondQuote));
                    double newLat = Double.parseDouble(line.substring(thirdQuote+1,fourthQuote));

                    // check if XMLCreator is printing anything, scanning is correct
                    Log.i("GPX_FILE","Lat: "+newLat+", Lon: "+newLon);

                    if (!(lon==0 && lat==0)){
                        // find Euclidean/Pythagorean distance b/w two points in miles, add to dist
                        double coordDist = Math.sqrt(((lon-newLon)*(lon-newLon))+((lat-newLat)*(lat-newLat)));
                        dist += COORD_TO_MILE*coordDist;
                    }

                    lon = newLon;
                    lat = newLat;
                }
            }
        }catch (IOException e){
            Log.wtf("GPX_READER","Failed to read GPX from SD card.");
        }

        // Converts miles to km before returning
        return dist * MILE_TO_KM;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();


    }

    @Override
    public void onBackPressed(){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(FinishedActivity.this);
        SharedPreferences.Editor editor = preferences.edit();

        // Ethan
        editor.putBoolean("currentlyTracking", false);
        editor.apply();

        Intent intent = new Intent(FinishedActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}
