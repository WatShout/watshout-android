package com.watshout.watshout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

class FriendDataManager {

    private final long TEN_MEGABYTE = 10 * 1024 * 1024;

    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    // [theirUID, isUserTracking]
    private HashMap<String, MapPlotter> mapPlotterList;
    private boolean firstTime;
    private Bitmap profilePic;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();

    private RecyclerView recyclerView;
    private RecyclerView.Adapter adapter;

    private List<MapFriendItem> mapFriendItems;

    private Context context;

    private MapRecycleViewCarrier carrier;
    private HashMap<String, IndividualFriendData> friendDataHashMap;

    FriendDataManager(String uid, final GoogleMap googleMap, final RecyclerView recyclerView, final Context context,
                      final MapRecycleViewCarrier carrier) {

        mapPlotterList = new HashMap<>();
        this.firstTime = true;
        this.recyclerView = recyclerView;
        this.context = context;
        mapFriendItems = new ArrayList<>();
        this.carrier = carrier;
        friendDataHashMap = new HashMap<>();

        final RecyclerView mRecyclerView = carrier.getRecyclerView();
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(context));

        Log.d("FRIENDS", mapFriendItems.toString());

        ref.child("friend_data").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String theirUID = dataSnapshot.getKey();

                Log.d("FRIENDSS", theirUID);

                ref.child("users").child(theirUID).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        String name = (String) dataSnapshot.getValue();

                        IndividualFriendData thisFriend = new IndividualFriendData(name, theirUID, mapPlotterList,
                                googleMap, recyclerView, context, mapFriendItems);

                        thisFriend.startTrackingLocation();
                        thisFriend.listenForStop();

                        friendDataHashMap.put(theirUID, thisFriend);
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
