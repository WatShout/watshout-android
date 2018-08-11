package com.watshout.watshout;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

public class UploadToDatabase {

    private String uid;
    private String distance;
    private String pace;
    private String currentDate;
    private int timeElapsed;
    private String mapUrl;

    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    UploadToDatabase(){
        this.currentDate = createFormattedDate();
    }

    UploadToDatabase(String uid){
        this.uid = uid;
        this.currentDate = createFormattedDate();
    }

    UploadToDatabase(String uid, String distance, String pace, int timeElapsed, String mapUrl){
        this.uid = uid;
        this.distance = distance;
        this.pace = pace;
        this.mapUrl = mapUrl;
        this.timeElapsed = timeElapsed;
        this.currentDate = createFormattedDate();
    }

    // Get date in format 'tue-may-29-04-58-14-gmt-00-00-2018'
    private String createFormattedDate() {

        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String interDate = date.toString();
        String fullDate = interDate.replaceAll("[^A-Za-z0-9]+", "-").toLowerCase();

        return fullDate;

    }

    public String getFormattedDate() {
        return this.currentDate;
    }

    // Creates data to be put into activity entry
    private EventInfo createMetadata(String type) {
        long time = System.currentTimeMillis();
        return new EventInfo(type, time, distance, pace, mapUrl, timeElapsed);

    }

    // Takes the 'current' activity and places it as an entry in
    // the 'past' entry
    public void moveCurrentToPast(final String date) {

        ref.child("users").child(uid).child("device").child("current")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        EventInfo metadata = createMetadata("run");

                        ref.child("users").child(uid).child("device").child("past")
                                .child(date).setValue(metadata);

                        removeCurrentEntry();
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    public void removeCurrentEntry() {
        ref.child("users").child(uid).child("device").child("current").removeValue();
    }
}
