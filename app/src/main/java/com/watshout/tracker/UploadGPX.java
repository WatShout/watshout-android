package com.watshout.tracker;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

public class UploadGPX {

    private Context context;

    private String uid;
    private String date;
    private File gpx;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();



    UploadGPX(Context context, String uid, String date, File gpx){
        this.context = context;
        this.uid = uid;
        this.date = date;
        this.gpx = gpx;
    }

    private byte[] fileToBytes(File gpx) throws IOException {

        return FileUtils.readFileToByteArray(gpx);

    }

    public void uploadToFirebaseStorage() throws IOException {

        byte[] bytes = fileToBytes(gpx);

        String fileName = date + ".gpx";

        storageReference.child("users").child(uid).child("gpx").child(fileName)
                .putBytes(bytes).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                Log.d("GPS", "File uploaded!");

                RequestQueue queue = Volley.newRequestQueue(context);
                String url = "https://watshout.herokuapp.com/mobile/strava/" + uid + "/" + date + "/";

                // Request a string response from the provided URL.
                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {

                                Toast.makeText(context,
                                        "Uploaded to Strava!",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context,
                                "Strava upload failed",
                                Toast.LENGTH_SHORT).show();
                    }
                });
                queue.add(stringRequest);
            }
        });


    }



}
