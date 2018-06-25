package com.watshout.tracker;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.util.Set;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class SettingsActivity extends AppCompatActivity implements IPickResult {

    final long TEN_MEGABYTE = 10 * 1024 * 1024;

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();

    String uid = thisUser.getUid();
    String email = thisUser.getEmail();
    String name = thisUser.getDisplayName();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();


    ImageView mProfile;
    EditText mEmail;
    EditText mAge;
    Button mEmailButton;
    Button mAgeButton;

    String fileFormat;
    String fileName;

    final int MY_PERMISSIONS_REQUEST_CAMERA = 505;
    String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mProfile = findViewById(R.id.profile);
        mEmail = findViewById(R.id.email);
        mAge = findViewById(R.id.age);
        mEmailButton = findViewById(R.id.email_button);
        mAgeButton = findViewById(R.id.age_button);

        checkCameraPermissions();

        ref.child("users").child(uid).child("age").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mAge.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PickImageDialog.build(new PickSetup()).show(SettingsActivity.this);

                }
        });

        ref.child("users").child(uid).child("profile_pic_format").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null){
                    fileFormat = dataSnapshot.getValue().toString();
                    fileName = "profile." + fileFormat;

                    storageReference.child("users").child(uid).child(fileName).getBytes(TEN_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {

                            Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            mProfile.setImageBitmap(bmp);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mAgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAge.getText().toString().length() > 0){

                    int newAge = Integer.parseInt(mAge.getText().toString());
                    ref.child("users").child(uid).child("age").setValue(newAge);

                    InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                    mAge.clearFocus();
                    mAge.setCursorVisible(false);

                }
            }
        });
    }


    byte[] getImageData(Bitmap bmp) {

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao); // bmp is bitmap from user image file
        bmp.recycle();
        byte[] byteArray = bao.toByteArray();

        return byteArray;
    }

    @Override
    public void onPickResult(PickResult r) {
        if (r.getError() == null) {

            Bitmap bmp1 = r.getBitmap();
            Bitmap bmp2 = bmp1.copy(bmp1.getConfig(), true);

            mProfile.setImageBitmap(bmp1);

            final byte[] data = getImageData(bmp2);

            if (fileName != null){
                storageReference.child("users").child(uid).child(fileName)
                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        storageReference.child("users").child(uid).child("profile.png")
                                .putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                Log.d("IMG", "Done uploading!");

                            }
                        }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                ref.child("users").child(uid).child("profile_pic_format").setValue("png");

                            }
                        });

                    }
                });
            } else {
                storageReference.child("users").child(uid).child("profile.png")
                        .putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        Log.d("IMG", "Done uploading!");

                    }
                }).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        ref.child("users").child(uid).child("profile_pic_format").setValue("png");

                    }
                });
            }


        } else {
            Log.d("IMG", "This didn't work: " + r.getError().toString());
        }
    }

    public void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    permissions,
                    MY_PERMISSIONS_REQUEST_CAMERA);


        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Open
                    // camera, take photo.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }
}
