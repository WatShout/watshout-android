package com.watshout.tracker;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.alternativevision.gpx.beans.GPX;

import java.util.Calendar;
import java.util.Date;

public class UploadFinishedActivity {

    private String uid;
    private String currentDate;

    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    UploadFinishedActivity(String uid){
        this.uid = uid;
        this.currentDate = createFormattedDate();
    }


    private String createFormattedDate() {
        // Get date in format 'tue-may-29-04-58-14-gmt-00-00-2018'
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String interDate = date.toString();
        String fullDate = interDate.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();

        return fullDate;

    }

    public String getFormattedDate() {
        return this.currentDate;
    }

    private EventInfo createMetadata(String type) {

        long time = System.currentTimeMillis();
        return new EventInfo(type, time);

    }


    public void moveCurrentToPast() {

        ref.child("users").child(uid).child("device").child("current")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        DataSnapshot currentRun = dataSnapshot;
                        EventInfo metadata = createMetadata("run");

                        String date = currentDate;

                        ref.child("users").child(uid).child("device").child("past")
                                .child(date).setValue(metadata);

                        ref.child("users").child(uid).child("device").child("past")
                                .child(date).child("path")
                                .setValue(currentRun.getValue());

                        removeCurrentEntry();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void removeCurrentEntry() {
        ref.child("users").child(uid).child("device").child("current").removeValue();
    }


}
