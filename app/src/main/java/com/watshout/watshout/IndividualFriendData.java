package com.watshout.watshout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
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

    private List<MapFriendItem> mapFriendItems;

    private int listSize;

    IndividualFriendData(String name, String uid, HashMap mapPlotterList, GoogleMap googleMap,
                         RecyclerView recyclerView, Context context,
                         List mapFriendItems){

        this.name = getInitials(name);
        this.uid = uid;
        this.mapPlotterList = mapPlotterList;
        this.googleMap = googleMap;
        this.recyclerView = recyclerView;
        this.context = context;
        this.mapFriendItems = mapFriendItems;

        firstEntry = true;

    }

    private ChildEventListener locationUpdates = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Log.d("FRIEND", "This works now");

            try {
                final double lat = dataSnapshot.child("lat").getValue(Double.class);
                final double lon = dataSnapshot.child("lon").getValue(Double.class);

                // User just started tracking location
                if (firstEntry){

                    listSize = mapFriendItems.size();

                    mapFriendItems.add(new MapFriendItem(name, new LatLng(lat, lon)));
                    RecyclerView.Adapter adapter = new MapFriendAdapter(mapFriendItems, context, googleMap);
                    recyclerView.setAdapter(adapter);

                    firstEntry = false;

                    Log.d("FIRST", "First time run");

                    mapPlotterList.put(uid, new MapPlotter(new ArrayList<Marker>(), googleMap, false, uid,
                            context));
                    mapPlotterList.get(uid).addFriendMarker(lat, lon);

                } else {

                    mapFriendItems.get(listSize).setCoords(new LatLng(lat, lon));

                    Log.d("FIRST", "Not first");
                    mapPlotterList.get(uid).addFriendMarker(lat, lon);

                    RecyclerView.Adapter adapter = new MapFriendAdapter(mapFriendItems, context, googleMap);
                    recyclerView.setAdapter(adapter);

                }
            } catch (NullPointerException e){
                Log.e("ERROR", e.toString());
            }

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
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

                // Remove user from the map
                if (dataSnapshot.getKey().equals("current")){

                    firstEntry = true;

                    try {
                        mapPlotterList.get(uid).removeFromMap();
                        mapPlotterList.remove(uid);
                        mapFriendItems.remove(listSize);
                        RecyclerView.Adapter adapter = new MapFriendAdapter(mapFriendItems, context, googleMap);
                        recyclerView.setAdapter(adapter);
                    } catch (NullPointerException e){
                        mapPlotterList.remove(uid);
                        mapFriendItems.remove(listSize);
                        RecyclerView.Adapter adapter = new MapFriendAdapter(mapFriendItems, context, googleMap);
                        recyclerView.setAdapter(adapter);
                    } catch (IndexOutOfBoundsException e){
                        mapPlotterList.remove(uid);
                        RecyclerView.Adapter adapter = new MapFriendAdapter(new ArrayList<MapFriendItem>(), context, googleMap);
                        recyclerView.setAdapter(adapter);
                    }



                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
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

    public String getInitials(String text) {
        String firstLetters = "";
        text = text.replaceAll("[.,]", ""); // Replace dots, etc (optional)
        for(String s : text.split(" "))
        {
            firstLetters += s.charAt(0);
        }
        return firstLetters;
    }


}
