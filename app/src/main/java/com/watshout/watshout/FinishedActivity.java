package com.watshout.watshout;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FinishedActivity extends AppCompatActivity{

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String uid = thisUser.getUid();
    String date;

    Boolean hasStrava;
    Boolean wantsToUploadStrava;

    CheckBox stravaCheckBox;
    Button returnToMap;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);

        stravaCheckBox = findViewById(R.id.stravaBox);
        returnToMap = findViewById(R.id.returnToMap);

        // load bitmap as byte array
        byte[] bitmapdata = getIntent().getByteArrayExtra("MAP_IMAGE");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);

        ImageView displayPath = findViewById(R.id.finishedRun);
        displayPath.setImageBitmap(bitmap);

        // load time and distance data
        int min = getIntent().getIntExtra("MIN",0);
        int sec = getIntent().getIntExtra("SEC",0);

        // find distance data from GPX file on SD card
        double dist = findDistanceFromGpx(getIntent().getStringExtra("GPX_NAME"));

        // display time data
        TextView time = findViewById(R.id.time);

        time.setText("" + min + "m " + sec +"s");

        // display pace and distance data
        TextView distance = findViewById(R.id.distance);
        TextView pace = findViewById(R.id.pace);
        distance.setText(String.format("%5.2f",dist)+" miles");
        pace.setText(String.format("%5.2f",((3600*dist)/((min*60)+sec)))+" mph");

        // load GPX from carrier class
        final XMLCreator XMLCreator = Carrier.getXMLCreator();

        // Get GPX file name from Intent
        date = getIntent().getStringExtra("GPX_NAME");
        date = date.substring(0, date.length() - 4);

        // Get value to determine whether or nlt to show checkbox
        hasStrava = Boolean.valueOf(getIntent().getStringExtra("STRAVA"));

        if (!hasStrava) {
            stravaCheckBox.setVisibility(View.INVISIBLE);
        }

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

                    UploadToDatabase uploadToDatabase = new UploadToDatabase(uid);
                    uploadToDatabase.moveCurrentToPast(date);

                    // Upload GPX to Firebase Storage
                    XMLCreator.uploadToFirebaseStorage(date, wantsToUploadStrava);
                    XMLCreator.resetXML();


                }catch (IOException e){e.printStackTrace();}

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

    public double findDistanceFromGpx(String fileName){

        File path = this.getExternalFilesDir(null);
        File file = new File(path, fileName);

        BufferedReader reader;

        final double COORD_TO_MILE = 69.172;
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

        return dist;

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(FinishedActivity.this,MainActivity.class);
        startActivity(intent);
        finish();
    }

}
