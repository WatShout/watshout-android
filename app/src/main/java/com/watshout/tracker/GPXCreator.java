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
import java.util.HashSet;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

public class GPXCreator {
    GPX gpxObject;
    Context context;
    String uid;

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
        GPXParser parser = new GPXParser();
        try {

            final String time = Long.toString(System.currentTimeMillis());

            String fileName = time + ".gpx";
            File path = context.getExternalFilesDir(null);
            Log.wtf("GPS", path.toString());
            File file = new File(path, fileName);
            path.mkdirs();

            FileOutputStream outStream = new FileOutputStream(file);
            if (!file.exists()) {
                file.createNewFile();
            }

            parser.writeGPX(gpxObject, outStream);

            byte bytes[] = FileUtils.readFileToByteArray(file);

            storageReference.child("users").child(uid).child("gpx").child(fileName)
                    .putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    Log.d("GPS", "File uploaded!");

                    RequestQueue queue = Volley.newRequestQueue(context);
                    String url = "https://watshout.herokuapp.com/mobile/strava/" + uid + "/" + time + "/";

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

                        }
                    });
                    queue.add(stringRequest);
                }
            });

            outStream.close();
        } catch (FileNotFoundException e) {
            Log.e("GPS", e.toString());
            e.printStackTrace();
        } catch (IOException e) {
            Log.e("GPS", e.toString());
            e.printStackTrace();
        } catch (TransformerException e) {
            Log.e("GPS", e.toString());
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            Log.e("GPS", e.toString());
            e.printStackTrace();
        }
    }

    public void resetGPXObject() {
        gpxObject = new GPX();
    }
}