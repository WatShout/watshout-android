package com.watshout.tracker;

import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class FriendData {

    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    // [theirUID, isUserTracking]
    private HashMap<String, Boolean> friendsList;
    private HashMap<String, MapPlotter> mapPlotterList;
    private String uid;

    FriendData (String uid) {

        this.uid = uid;
        friendsList = new HashMap<>();

        ref.child("friend_data").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String theirUID = dataSnapshot.getKey();

                ref.child("users").child(theirUID).child("device").child("current").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d("FRIEND", Boolean.toString(dataSnapshot.getValue() == null));
                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    public HashMap<String, Boolean> getFriendsList() {
        return friendsList;
    }

}
