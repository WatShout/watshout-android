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

    private Context context;
    private String uid;
    private GPX gpxObject;

    GPXCreator(Context context, String uid) {
        gpxObject = new GPX();
        this.context = context;
        this.uid = uid;
    }

    public void addTrack(Track t) {
        gpxObject.addTrack(t);
    }

    public void writeGPXFile(String date) throws IOException,
            TransformerException,
            ParserConfigurationException {

        GPXParser parser = new GPXParser();

        // These lines of code write the file locally
        String fileName = date + ".gpx";

        File path = context.getExternalFilesDir(null);
        File file = new File(path, fileName);
        path.mkdirs();
        FileOutputStream outStream = new FileOutputStream(file);
        parser.writeGPX(gpxObject, outStream);
        outStream.close();

        UploadGPX uploadGPX = new UploadGPX(context,
                uid, date, file);

        // Note: This also makes the call to Strava
        uploadGPX.uploadToFirebaseStorage();

    }

    public void resetGPXObject() {
        gpxObject = new GPX();
    }
}