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

public class FriendData {

    final long TEN_MEGABYTE = 10 * 1024 * 1024;

    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    // [theirUID, isUserTracking]
    private HashMap<String, Boolean> friendsList;
    private HashMap<String, MapPlotter> mapPlotterList;
    private String uid;
    private GoogleMap googleMap;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    FriendData (String uid, final GoogleMap googleMap) {

        this.uid = uid;
        this.googleMap = googleMap;
        friendsList = new HashMap<>();
        mapPlotterList = new HashMap<>();

        ref.child("friend_data").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final String theirUID = dataSnapshot.getKey();

                ref.child("users").child(theirUID).child("device").child("current").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(final DataSnapshot dataSnapshot, String s) {

                        // User just started tracking location
                        if (mapPlotterList.get(theirUID) == null){

                            ref.child("users").child(theirUID).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot profilePicSnapshot) {

                                    String profilePicFormat = profilePicSnapshot.child("profile_pic_format").getValue(String.class);
                                    Log.d("FRIEND", profilePicFormat);


                                    storageReference.child("users").child(theirUID).child("profile." + profilePicFormat).getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                        @Override
                                        public void onSuccess(byte[] bytes) {

                                            Bitmap profilePic = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                                            int height = 80;
                                            int width = 60;

                                            profilePic = Bitmap.createScaledBitmap(profilePic, width, height, false);

                                            mapPlotterList.put(theirUID, new MapPlotter(new ArrayList<Marker>(), googleMap, false, profilePic));

                                            double lat = dataSnapshot.child("lat").getValue(Double.class);
                                            double lon = dataSnapshot.child("lon").getValue(Double.class);

                                            mapPlotterList.get(theirUID).addFriendMarker(lat, lon);

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            Log.d("FRIEND", exception.toString());
                                        }
                                    });
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        } else {

                            double lat = dataSnapshot.child("lat").getValue(Double.class);
                            double lon = dataSnapshot.child("lon").getValue(Double.class);

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

    public HashMap<String, Boolean> getFriendsList() {
        return friendsList;
    }

}
