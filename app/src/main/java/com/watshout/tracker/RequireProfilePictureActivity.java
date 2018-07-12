package com.watshout.tracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

import java.io.ByteArrayOutputStream;

public class RequireProfilePictureActivity extends AppCompatActivity{

    public static final int GET_FROM_GALLERY = 3;
    public static final String TAG = "PfpRetrieval";
    public final Context context = this;

    private DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_require_pfp);
    }

    public void uploadProfilePicture(View v){
        startActivityForResult(
                new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.INTERNAL_CONTENT_URI
                ),
                GET_FROM_GALLERY
        );
    }

    public void continueWithoutProfilePicture(View v){goToMainActivity();}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);
        try {
            switch (requestCode) {

                case GET_FROM_GALLERY:
                    if (resultCode == Activity.RESULT_OK) {
                        //data gives you the image uri. Try to convert that to bitmap
                        //convert data to bitmap, display in ImageView
                        Uri imageUri = data.getData();
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        ImageView tv1;
                        tv1= (ImageView) findViewById(R.id.profilePictureDisplay);
                        tv1.setImageBitmap(bmp);

                        // update button text
                        Button uploadButton = (Button) findViewById(R.id.uploadPfpButton);
                        uploadButton.setText("Select new profile picture");

                        Button continueButton = (Button) findViewById(R.id.continueButton);
                        continueButton.setText("Continue");

                        // TODO upload bitmap/URI to firebase storage
                        FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
                        String uid = thisUser.getUid();

                        uploadBitmapAsProfilePicture(bmp,uid);

                        break;
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        Log.e(TAG, "Selecting picture cancelled");
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in onActivityResult : " + e.getMessage());
        }

    }

    public void uploadBitmapAsProfilePicture(Bitmap bitmap,String uid){

        FirebaseStorage storage = FirebaseStorage.getInstance();
        final String finalUid = uid;

        // Create a storage reference from our app
        StorageReference storageRef = storage.getReferenceFromUrl("gs://watshout-app.appspot.com");

        // Create a reference to profile picture
        StorageReference pfpRef = storageRef.child("users/"+uid+"/profile.jpeg");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = pfpRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                Toast.makeText(context,"Failed to upload image",Toast.LENGTH_LONG);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Toast.makeText(context,"Upload successful!",Toast.LENGTH_LONG);
                ref.child("users").child(finalUid).child("profile_pic_format").push();
                ref.child("users").child(finalUid).child("profile_pic_format").setValue("jpeg");
            }
        });

    }

    public void goToMainActivity(){

        Intent openMain = new Intent(getApplicationContext(), MainActivity.class);
        openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(openMain);
        finish();

    }

}
