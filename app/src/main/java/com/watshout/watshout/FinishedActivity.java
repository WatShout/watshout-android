package com.watshout.watshout;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
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

    Boolean hasStrava;
    Boolean wantsToUploadStrava;

    CheckBox stravaCheckBox;
    Button returnToMap;
    TextView time;
    TextView distance;
    TextView pace;
    String mapURL;
    ImageView mFinishedRun;

    // General database reference
    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();


    void loadMapImage() {
        Picasso.get()
                .load(mapURL)
                .into(mFinishedRun);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);

        stravaCheckBox = findViewById(R.id.stravaBox);
        returnToMap = findViewById(R.id.returnToMap);
        time = findViewById(R.id.time);
        distance = findViewById(R.id.distance);
        pace = findViewById(R.id.pace);
        mFinishedRun = findViewById(R.id.finishedRun);

        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
        String units = settings.getString("Units", "Metric");

        // Get value to determine whether or nlt to show checkbox
        hasStrava = Boolean.valueOf(getIntent().getStringExtra("STRAVA"));
        mapURL = getIntent().getStringExtra("MAP_URL");

        loadMapImage();

        if (!hasStrava) {
            stravaCheckBox.setVisibility(View.INVISIBLE);
        }

        // load time and distance data
        final int min = getIntent().getIntExtra("MIN",0);
        final int sec = getIntent().getIntExtra("SEC",0);

        DecimalFormat formatter = new DecimalFormat("00");
        String formattedMin = formatter.format(min);
        String formattedSec = formatter.format(sec);

        time.setText("Time: " + formattedMin + ":" + formattedSec);

        final double rawMetricDistance = findDistanceFromGpx(getIntent().getStringExtra("GPX_NAME"));

        final PaceCalculator paceCalculator = new PaceCalculator(rawMetricDistance, min, sec);

        if (units.equals("Metric")){
            distance.setText("Distance: " + paceCalculator.getMetricDistance());
            pace.setText("Pace: " + paceCalculator.getMetricPace());
        } else {
            distance.setText("Distance: " + paceCalculator.getImperialDistance());
            pace.setText("Pace: " + paceCalculator.getImperialPace());
        }

        // load GPX from carrier class
        final XMLCreator XMLCreator = Carrier.getXMLCreator();

        // Get GPX file name from Intent
        date = getIntent().getStringExtra("GPX_NAME");
        date = date.substring(0, date.length() - 4);

        Button uploadGpx = findViewById(R.id.uploadGpx);
        uploadGpx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final ProgressDialog progressDialog = new ProgressDialog(FinishedActivity.this);
                progressDialog.setMessage("Uploading run data...");
                progressDialog.show();

                // If user checked box, then upload to Strava
                wantsToUploadStrava = stravaCheckBox.isChecked();

                try {

                    UploadToDatabase uploadToDatabase = new UploadToDatabase(uid,
                            paceCalculator.getMetricDistance(),
                            paceCalculator.getMetricPace(),
                            mapURL);

                    uploadToDatabase.moveCurrentToPast(date);

                    // Upload GPX to Firebase Storage
                    XMLCreator.uploadToFirebaseStorage(date, wantsToUploadStrava);
                    XMLCreator.resetXML();


                } catch (IOException e){
                    e.printStackTrace();
                }

                progressDialog.dismiss();

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
    public void onBackPressed(){
        Intent intent = new Intent(FinishedActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}
