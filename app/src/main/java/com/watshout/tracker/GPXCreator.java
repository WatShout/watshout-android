package com.watshout.tracker;

import android.content.Context;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.alternativevision.gpx.GPXParser;
import org.alternativevision.gpx.beans.GPX;
import org.alternativevision.gpx.beans.Track;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class GPXCreator {
    GPX gpxObject;
    Context context;
    String uid;

    // General database reference
    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    public GPXCreator(Context context, String uid) {
        gpxObject = new GPX();
        this.context = context;
        this.uid = uid;
    }

    public void addTrack(Track t) {
        gpxObject.addTrack(t);
    }

    public void writeGPXFile() {
        final GPXParser parser = new GPXParser();

        ref.child("users").child(uid).child("device").child("current").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Get date in format 'tue-may-29-04-58-14-gmt-00-00-2018'
                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                String interDate = date.toString();
                final String fullDate = interDate.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();

                // Reference is pointing to the entry for the 'finished activity
                DatabaseReference specificRef = ref
                        .child("users")
                        .child(uid)
                        .child("device")
                        .child("past")
                        .child(fullDate);

                long time = System.currentTimeMillis();

                // Creates a new object with activity metadata
                EventInfo thisEventInfo = new EventInfo("run", time);

                // Adds the metadata/EventInfo to the new child of the database
                specificRef.setValue(thisEventInfo);

                // Adds the 'current' activity to the path subfolder
                specificRef.child("path").setValue(dataSnapshot.getValue());

                // Removes the current activity
                ref.child("users").child(uid).child("device").child("current").removeValue();

                String fileName = fullDate + ".gpx";

                File path = context.getExternalFilesDir(null);
                Log.wtf("GPS", path.toString());
                File file = new File(path, fileName);
                path.mkdirs();

                FileOutputStream outStream = null;
                try {
                    outStream = new FileOutputStream(file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                if (!file.exists()) {
                    try {
                        file.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                try {
                    parser.writeGPX(gpxObject, outStream);
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (TransformerException e) {
                    e.printStackTrace();
                }

                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                byte bytes[] = new byte[0];
                try {
                    bytes = FileUtils.readFileToByteArray(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                storageReference.child("users").child(uid).child("gpx").child(fileName)
                        .putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        Log.d("GPS", "File uploaded!");

                        RequestQueue queue = Volley.newRequestQueue(context);
                        String url = "https://watshout.herokuapp.com/mobile/strava/" + uid + "/" + fullDate + "/";

                        // Request a string response from the provided URL.
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                                new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        Log.d("GPS", "Uploaded to Strava");
                                    }
                                }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.d("GPS", "Strava upload failed");
                            }
                        });
                        queue.add(stringRequest);
                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    public void resetGPXObject() {
        gpxObject = new GPX();
    }
}