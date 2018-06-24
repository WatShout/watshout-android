package com.watshout.tracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
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

        Button btnCamera = (Button)findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 0);
            }
        });
        imageView = (ImageView)findViewById(R.id.imageView);
        btnReturn = (Button)findViewById(R.id.btnReturn);
        btnReturn.setVisibility(View.INVISIBLE);
        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Bitmap to byte array
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                bitmap.recycle();

                // Uploading image to Firebase Storage
                Date currentTime = Calendar.getInstance().getTime();
                String firebaseImageName = (currentTime.getMonth()+1) + "-" + currentTime.getDate() + "-" + (1900+currentTime.getYear()) + "-" + currentTime.getHours() + ":" + currentTime.getMinutes() + ":" + currentTime.getSeconds();
                StorageReference imageRef = mStorageRef.child("users/" + uid+"/userImages/"+firebaseImageName);

                imageRef.putBytes(byteArray)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Log.e("Upload", "Successful");
                                Toast.makeText(getApplicationContext(), "Image uploaded!", Toast.LENGTH_LONG).show();
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

                // Uploading image info to Firebase Database
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("users/"+uid+"/imageInfo");

                myRef.setValue(firebaseImageName);


                Log.e("Time:", currentTime.getHours() + ":" + currentTime.getMinutes());
                Log.e("Date:",(currentTime.getMonth()+1) + "/" + currentTime.getDate() + "/" + (1900+currentTime.getYear()));
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
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
