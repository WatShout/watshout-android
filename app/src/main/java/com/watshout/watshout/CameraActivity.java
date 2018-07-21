package com.watshout.watshout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class CameraActivity extends AppCompatActivity {
    ImageView imageView;
    Button btnReturn;
    Bitmap bitmap;
    String uid;

    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        uid = getIntent().getStringExtra("uid");

        final Activity myActivity = this;

        Button btnCamera = (Button)findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE); // pickImageDIalogue
                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), "Camera permission is not enabled, please enable it through Settings.", Toast.LENGTH_SHORT).show();
                    ActivityCompat.requestPermissions(myActivity,
                            new String[]{Manifest.permission.CAMERA}, 200);
                }
                else {
                    startActivityForResult(intent, 0);
                }
            }
        });
        imageView = (ImageView)findViewById(R.id.imageView);
        btnReturn = (Button)findViewById(R.id.btnReturn);
        btnReturn.setVisibility(View.INVISIBLE);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getApplicationContext(), "Uploading image...", Toast.LENGTH_LONG).show();

                // Bitmap to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                bitmap.recycle();

                // Uploading image to Firebase Storage
                final Date currentTime = Calendar.getInstance().getTime();
                final String firebaseImageName = (currentTime.getMonth()+1) + "-" + currentTime.getDate() + "-" + (1900+currentTime.getYear()) + "-" + currentTime.getHours() + ":" + currentTime.getMinutes() + ":" + currentTime.getSeconds();
                final StorageReference imageRef = mStorageRef.child("users/" + uid+"/activityImages/"+firebaseImageName);
                // Upload image
                imageRef.putBytes(byteArray)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // Add metadata
                                StorageMetadata imageData = new StorageMetadata.Builder()
                                        .setCustomMetadata("date", (currentTime.getMonth()+1) + "-" + currentTime.getDate() + "-" + (1900+currentTime.getYear()))
                                        .setCustomMetadata("time", currentTime.getHours() + ":" + currentTime.getMinutes() + ":" + currentTime.getSeconds())
                                        .setCustomMetadata("location", FusedLocation.latitude + "," + FusedLocation.longitude)
                                        .build();

                                imageRef.updateMetadata(imageData)
                                        .addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                                            @Override
                                            public void onSuccess(StorageMetadata storageMetadata) {
                                                Log.e("Metadata", "Success");
                                                // Uploading some data to Firebase Database, for easy event listening
                                                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                                                rootRef.child("users").child(uid).child("activityImages").child(firebaseImageName).setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getApplicationContext(), "Image uploaded!", Toast.LENGTH_LONG).show();
                                                        Log.e("Time:", currentTime.getHours() + ":" + currentTime.getMinutes());
                                                        Log.e("Date:",(currentTime.getMonth()+1) + "/" + currentTime.getDate() + "/" + (1900+currentTime.getYear()));
                                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                                        startActivity(intent);
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getApplicationContext(), "Image data was not uploaded to database, please try again.", Toast.LENGTH_LONG).show();
                                                    }
                                                });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.e("Metadata", "Failure");
                                            }
                                        });
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                // Exception
                                Toast.makeText(getApplicationContext(), "Image not uploaded, please try again.", Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        });
            }
        });

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        bitmap = (Bitmap)data.getExtras().get("data");
        imageView.setImageBitmap(bitmap);
        btnReturn.setVisibility(View.VISIBLE);
    }
}
