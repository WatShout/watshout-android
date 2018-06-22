package com.watshout.tracker;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
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

class FriendData {

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

    FriendData (String uid, final GoogleMap googleMap) {

        mapPlotterList = new HashMap<>();
        this.firstTime = true;

        ref.child("friend_data").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String theirUID = dataSnapshot.getKey();

                ref.child("users").child(theirUID).child("device").child("current").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                        final double lat = dataSnapshot.child("lat").getValue(Double.class);
                        final double lon = dataSnapshot.child("lon").getValue(Double.class);

                        // User just started tracking location
                        if (firstTime){

                            firstTime = false;

                            Log.d("FRIEND", "First time run");

                            mapPlotterList.put(theirUID, new MapPlotter(new ArrayList<Marker>(), googleMap, false));
                            mapPlotterList.get(theirUID).addFriendMarker(lat, lon);

                            ref.child("users").child(theirUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot profilePicSnapshot) {

                                    String profilePicFormat = profilePicSnapshot.child("profile_pic_format").getValue(String.class);

                                    storageReference.child("users").child(theirUID).child("profile." + profilePicFormat).getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {

                                            Log.d("FRIEND", "Picture downloaded");

                                            profilePic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                            int height = 80;
                                            int width = 60;

                                            profilePic = Bitmap.createScaledBitmap(profilePic, width, height, false);

                                            mapPlotterList.get(theirUID).setProfilePic(profilePic);


                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {

                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                        } else {

                            mapPlotterList.get(theirUID).addFriendMarker(lat, lon);

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

}
