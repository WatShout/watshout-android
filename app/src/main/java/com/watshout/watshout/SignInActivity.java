package com.watshout.watshout;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.google.android.gms.signin.SignIn;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watshout.watshout.pojo.SuccessCheck;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends AppCompatActivity {

    String TAG = "LogIn";

    // Choose an arbitrary request code value
    private static final int RC_SIGN_IN = 123;
    FirebaseUser thisUser;
    FirebaseAuth mAuth;

    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    RetrofitInterface retrofitInterface = RetrofitClient
            .getRetrofitInstance().create(RetrofitInterface.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

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


        if (!isNetworkAvailable()){
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("No Network Connection Detected");
            builder.setMessage("The app will now close. Please re-open the app once you have a network connection");

            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    finishAffinity();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }

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
                            .setLogo(R.drawable.small_logo)
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

        final ProgressDialog progressDialog = new ProgressDialog(SignInActivity.this);
        progressDialog.setMessage("Signing in...");
        progressDialog.show();

        // RC_SIGN_IN is the request code you passed into startActivityForResult(...) when starting the sign in flow.
        if (requestCode == RC_SIGN_IN) {
            IdpResponse response = IdpResponse.fromResultIntent(data);

            // Successfully signed in
            if (resultCode == RESULT_OK) {

                thisUser = FirebaseAuth.getInstance().getCurrentUser();
                String uid = thisUser.getUid();
                String email = thisUser.getEmail();
                mAuth = FirebaseAuth.getInstance();

                Call<SuccessCheck> call = retrofitInterface.getEmailAuthorized(email);
                call.enqueue(new Callback<SuccessCheck>() {
                    @Override
                    public void onResponse(Call<SuccessCheck> call, Response<SuccessCheck> response) {

                        boolean authorized = response.body().getSuccess();

                        if (authorized){

                            // if Firebase user doesn't have profile_pic_format, open InitializeNewAccountActivity
                            ref.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild("profile_pic_format")) {
                                        Carrier.setUploadedOwnProfilePicture(true);
                                        Intent openMain = new Intent(getApplicationContext(), MainActivity.class);
                                        openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        getApplicationContext().startActivity(openMain);
                                        finish();
                                    } else {
                                        Carrier.setUploadedOwnProfilePicture(false);
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
                        } else {

                            AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);

                            builder.setTitle("Not an approved user");
                            builder.setMessage("We are currently only open to .edu email users. " +
                                            "If you would like to request access to the app please " +
                                            "email accounts@watshout.com");

                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    thisUser.delete();
                                    mAuth.signOut();
                                    Intent reopenSignIn = new Intent(getApplicationContext(), SignInActivity.class);
                                    getApplicationContext().startActivity(reopenSignIn);
                                }
                            });

                            AlertDialog dialog = builder.create();
                            dialog.show();

                        }
                    }

                    @Override
                    public void onFailure(Call<SuccessCheck> call, Throwable t) {
                        Log.e(TAG, t.toString());
                    }
                });

            } else {
                // Sign in failed
                if (response == null) {
                    return;
                }

                if (response.getError().getErrorCode() == ErrorCodes.NO_NETWORK) {
                    return;
                }
            }
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}

