package com.watshout.app;

import android.graphics.Color;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Carrier {

    private static XMLCreator XMLCreator;
    private static boolean uploadedOwnProfilePicture = false;
    private static String[] colorNames = {"red","yellow","green","blue","magenta"};
    private static int[] colors = {Color.RED,Color.YELLOW,Color.GREEN,Color.BLUE, Color.MAGENTA};

    static void setXMLCreator(XMLCreator XMLCreatorIn){XMLCreator = XMLCreatorIn;}
    static XMLCreator getXMLCreator(){return XMLCreator;}

    static void setUploadedOwnProfilePicture(boolean pfpIn){uploadedOwnProfilePicture = pfpIn;}
    static boolean getUploadedOwnProfilePicture(){return uploadedOwnProfilePicture;}

    private static int out;

    static int getUserColor(String uid){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        ref.child("users").child(uid).child("color").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                out = getColorFromString(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return out;
    }

    static int getColorFromString(String color){
        for (int i=0;i<colorNames.length;i++){
            if (color.equals(colorNames[i]))
                return colors[i];
        }
        return colors[0];
    }

}