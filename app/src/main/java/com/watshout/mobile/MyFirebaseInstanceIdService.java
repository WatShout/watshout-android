package com.watshout.mobile;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "MyAndroidFCMIIDService";

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    @Override
    public void onTokenRefresh() {
        //Get hold of the registration token
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        //Log the token
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        // Send token to server
        sendRegistrationToServer(refreshedToken);
    }
    private void sendRegistrationToServer(String token) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("fcm_token", null);
        editor.apply();

        try {
            String uid = thisUser.getUid();
            ref.child("users").child(uid).child("fcm_token").setValue(token);
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }
}