package com.watshout.watshout;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class IndividualFriendData {

    private final long TEN_MEGABYTE = 10 * 1024 * 1024;

    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    private String name;
    private String uid;
    private HashMap<String, MapPlotter> mapPlotterList;

    private Boolean firstEntry;
    private GoogleMap googleMap;
    private RecyclerView recyclerView;
    private Context context;

    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageReference = storage.getReference();

    private Bitmap profilePic;

    private HashMap<String, FriendCurrentLocation> friendsCurrentlyOnMap;


    IndividualFriendData(String name, String uid, HashMap mapPlotterList, GoogleMap googleMap,
                         RecyclerView recyclerView, Context context,
                         HashMap<String, FriendCurrentLocation> friendsCurrentlyOnMap){

        this.name = getInitials(name);
        this.uid = uid;
        this.mapPlotterList = mapPlotterList;
        this.googleMap = googleMap;
        this.recyclerView = recyclerView;
        this.context = context;
        this.friendsCurrentlyOnMap = friendsCurrentlyOnMap;

        firstEntry = true;

    }

    private ChildEventListener locationUpdates = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            try {
                final double lat = dataSnapshot.child("lat").getValue(Double.class);
                final double lon = dataSnapshot.child("lon").getValue(Double.class);

                // User just started tracking location
                if (firstEntry){
                    Log.d("FIRST", "First time run");
                    firstEntry = false;

                    // Add new FriendCurrentLocation with the user's latest lat/lng
                    friendsCurrentlyOnMap.put(uid, (new FriendCurrentLocation(name, new LatLng(lat, lon))));

                    // Create a new adapter using the newly-updated HashMap. Set the adapter on the
                    // 'bubble sidebar'
                    RecyclerView.Adapter adapter = new MapFriendAdapter(friendsCurrentlyOnMap, context, googleMap);
                    recyclerView.setAdapter(adapter);

                    // Create a new entry in mapPlotterList with a new MapPlotter object that is specific
                    // to this user
                    mapPlotterList.put(uid, new MapPlotter(new ArrayList<Marker>(), googleMap, false, uid,
                            context));

                    // This actually plots the marker/polyline on the Google map object
                    mapPlotterList.get(uid).addFriendMarker(lat, lon);

                } else {

                    // Simply update the latest lat/lng values and plot them on the map
                    friendsCurrentlyOnMap.get(uid).setCoords(new LatLng(lat, lon));
                    mapPlotterList.get(uid).addFriendMarker(lat, lon);

                    // Set the adapter with new values
                    RecyclerView.Adapter adapter = new MapFriendAdapter(friendsCurrentlyOnMap, context, googleMap);
                    recyclerView.setAdapter(adapter);

                }
            } catch (NullPointerException e){
                Log.e("ERROR", e.toString());
            }

        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) { }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

        @Override
        public void onCancelled(DatabaseError databaseError) { }
    };

    public void startTrackingLocation() {

        ref.child("users").child(uid).child("device").child("current").addChildEventListener(locationUpdates);

    }

    private void stopTrackingLocation() {

        ref.child("users").child(uid).child("device").child("current").removeEventListener(locationUpdates);

    }

    public void listenForStop() {
        ref.child("users").child(uid).child("device").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                // Remove user from the map
                if (dataSnapshot.getKey().equals("current")){

                    firstEntry = true;

                    if (mapPlotterList.get(uid) != null){
                        // Remove entry from map
                        mapPlotterList.get(uid).removeFromMap();
                        mapPlotterList.remove(uid);
                    }

                    // Remove entry from map friend items
                    if (friendsCurrentlyOnMap.get(uid) != null){
                        friendsCurrentlyOnMap.remove(uid);
                    }

                    RecyclerView.Adapter adapter = new MapFriendAdapter(friendsCurrentlyOnMap, context, googleMap);
                    recyclerView.setAdapter(adapter);

                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) { }

            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });
    }

    private Bitmap getSquareBitmap(Bitmap bm) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        int narrowSize = Math.min(width, height);
        int differ = (int)Math.abs((bm.getHeight() - bm.getWidth())/2.0f);
        width  = (width  == narrowSize) ? 0 : differ;
        height = (width == 0) ? differ : 0;

        Bitmap resizedBitmap = Bitmap.createBitmap(bm, width, height, narrowSize, narrowSize);
        //bm.recycle();
        return resizedBitmap;
    }

    private String getInitials(String text) {
        String firstLetters = "";
        text = text.replaceAll("[.,]", ""); // Replace dots, etc (optional)
        for(String s : text.split(" "))
        {
            firstLetters += s.charAt(0);
        }
        return firstLetters;
    }


}
