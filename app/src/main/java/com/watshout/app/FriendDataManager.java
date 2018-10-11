package com.watshout.app;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

// This class is only instantiated once (should maybe be a singleton)
/*

This class manages the host user's currently tracking friends.

It starts by first retrieving a list of all of the user's friends and then creating a
"IndividualFriendData" class for them. This class then manages the map-plotting.

friendsCurrentlyOnMap is used for updating the latest location of friends

 */
class FriendDataManager {


    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    // [theirUID, isUserTracking]
    private HashMap<String, MapPlotter> mapPlotterList;

    // FriendCurrentLocation is a simple object for getting/setting a
    // user's most recent lat/lng (used for the sidebar button click)
    private CurrentlyTrackingFriendsHolder currentlyTrackingFriends =
            new CurrentlyTrackingFriendsHolder();

    FriendDataManager(String uid, final GoogleMap googleMap, final RecyclerView recyclerView, final Context context,
                      final MapRecycleViewCarrier carrier) {

        mapPlotterList = new HashMap<>();


        final RecyclerView mRecyclerView = carrier.getRecyclerView();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        ref.child("friend_data").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String theirUID = dataSnapshot.getKey();

                ref.child("users").child(theirUID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String name = (String) dataSnapshot.getValue();

                        IndividualFriendData thisFriend = new IndividualFriendData(name, theirUID, mapPlotterList,
                                googleMap, recyclerView, context, currentlyTrackingFriends);

                        thisFriend.startTrackingLocation();
                        thisFriend.listenForStop();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {}

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

    }
}
