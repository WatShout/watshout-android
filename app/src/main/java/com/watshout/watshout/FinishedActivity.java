package com.watshout.tracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class FinishedActivity extends AppCompatActivity{

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finished);

        // show finished path in an ImageView
        // TODO fix issue where bitmap is null

        // load bitmap directly
        //Bitmap bitmap = (Bitmap) getIntent().getParcelableExtra("MAP_IMAGE");
        //Log.i("Map_Display",bitmap.getWidth()+"x"+bitmap.getHeight());

        // load bitmap as byte array
        byte[] bitmapdata = getIntent().getByteArrayExtra("MAP_IMAGE");
        Bitmap bitmap = BitmapFactory.decodeByteArray(bitmapdata, 0, bitmapdata.length);

        ImageView displayPath = (ImageView) findViewById(R.id.finishedRun);
        displayPath.setImageBitmap(bitmap);

        // load time and distance data
        int min = getIntent().getIntExtra("MIN",0);
        int sec = getIntent().getIntExtra("SEC",0);

        // retrieve pre-calculated distance from MapFragment
        //double dist = getIntent().getDoubleExtra("DIST",0.0);

        // find distance data from GPX file on SD card
        double dist = findDistanceFromGpx(getIntent().getStringExtra("GPX_NAME"));

        // display time data
        TextView time = (TextView) findViewById(R.id.time);

        time.setText("" + min + "m " + sec +"s");

        // display pace and distance data
        TextView distance = (TextView) findViewById(R.id.distance);
        TextView pace = (TextView) findViewById(R.id.pace);
        distance.setText(String.format("%5.2f",dist)+" miles");
        pace.setText(String.format("%5.2f",((3600*dist)/((min*60)+sec)))+" mph");

        // load GPX from carrier class
        final XMLCreator XMLCreator = Carrier.getXMLCreator();

        // TODO test GPX upload after Firebase Storage gets up again
        Button uploadGpx = (Button) findViewById(R.id.uploadGpx);
        uploadGpx.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    // Upload GPX to Firebase Storage
                    XMLCreator.uploadToFirebaseStorage();
                    XMLCreator.resetXML();
                }catch (IOException e){e.printStackTrace();}
            }
        });

        Button returnToMap = (Button) findViewById(R.id.returnToMap);
        returnToMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
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

}
