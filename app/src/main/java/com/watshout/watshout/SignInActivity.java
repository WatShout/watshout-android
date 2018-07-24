package com.watshout.watshout;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SignInActivity extends AppCompatActivity {

    String TAG = "LogIn";

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;

    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String[] dangerousPermissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.GET_ACCOUNTS,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.CAMERA};

        // Go through permissions, check which ones aren't granted
        List<String> request = new ArrayList<>();
        for (String permission:dangerousPermissions){
            if (ContextCompat.checkSelfPermission(SignInActivity.this,
                    permission)
                    != PackageManager.PERMISSION_GRANTED) {

                // Permission is not granted
                if (ActivityCompat.shouldShowRequestPermissionRationale(SignInActivity.this,
                        permission)) {
                    // TODO Asynchronously show the rationale for needing permission

                }

                request.add(permission);
            }
        }

        if (request.size() > 0) {
            // Request the permission
            ActivityCompat.requestPermissions(SignInActivity.this,
                    request.toArray(new String[0]),
                    0);
        }

        setContentView(R.layout.activity_sign_in);

        // Removes the top bar on top of the map
        //getSupportActionBar().hide();

        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            Intent openMain = new Intent(getApplicationContext(), MainActivity.class);
            openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(openMain);
            finish();
        } else {
            startActivityForResult(
                    // Get an instance of AuthUI based on the default app
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setIsSmartLockEnabled(false)
                            .setTheme(R.style.LoginTheme)
                            .setAvailableProviders(Arrays.asList(
                                    new AuthUI.IdpConfig.EmailBuilder().build(),
                                    new AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    RC_SIGN_IN);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {

                FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = thisUser.getUid();

                // if Firebase user doesn't have profile_pic_format, open InitializeNewAccountActivity
                ref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("profile_pic_format")) {
                            Intent openMain = new Intent(getApplicationContext(), MainActivity.class);
                            openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(openMain);
                            finish();
                        } else {
                            Intent openPfp = new Intent(getApplicationContext(), InitializeNewAccountActivity.class);
                            openPfp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            getApplicationContext().startActivity(openPfp);
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Log.wtf(TAG, "It worked");
            } else {
                // Sign in failed
                if (response == null) {
                    Log.wtf(TAG, "User pressed back button");

                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    Log.wtf(TAG, "No network brahs");
                    return;
                }


                Log.wtf(TAG, "idk what happened here");
            }
        }
    }

}
