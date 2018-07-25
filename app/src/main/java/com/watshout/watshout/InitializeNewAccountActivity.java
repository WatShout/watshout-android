package com.watshout.watshout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;

public class InitializeNewAccountActivity extends AppCompatActivity {

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    final String uid = thisUser.getUid();
    final String email = thisUser.getEmail();
    final String name = thisUser.getDisplayName();

    public static final int GET_FROM_GALLERY = 3;
    public static final String TAG = "PfpRetrieval";
    public final Context context = this;
    ImageView mProfile;
    EditText mAge;

    boolean uploadedOwnPicture = false;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize_account);

        mProfile = findViewById(R.id.profilePictureDisplay);
        mAge = findViewById(R.id.age);

    }

    public void selectProfilePicture(View v){

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GET_FROM_GALLERY);

    }

    public void continueToMainActivity(View v){

        if (!uploadedOwnPicture) {

            ProgressDialog nDialog;
            nDialog = new ProgressDialog(InitializeNewAccountActivity.this);
            nDialog.setTitle("Uploading data to server");
            nDialog.setIndeterminate(false);
            nDialog.setCancelable(true);
            nDialog.show();

            Drawable drawable = getResources().getDrawable(R.drawable.blank_profile);
            Bitmap blankProfile = ((BitmapDrawable) drawable).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            blankProfile.compress(Bitmap.CompressFormat.PNG, 100, stream);
            byte[] data = stream.toByteArray();

            storageReference.child("users/" + uid + "/profile.png")
                    .putBytes(data).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                    ref.child("users").child(uid).child("profile_pic_format").setValue("png");
                    ref.child("users").child(uid).child("email").setValue(email);
                    ref.child("users").child(uid).child("name").setValue(name);
                    ref.child("users").child(uid).child("age").setValue(Integer.valueOf(mAge.getText().toString()));

                    goToMainActivity();

                }
            });
        } else {
            goToMainActivity();
        }
    }

    public void goToMainActivity(){

        Intent openMain = new Intent(getApplicationContext(), MainActivity.class);
        openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(openMain);
        finish();

    }

    byte[] getImageData(Bitmap bmp) {

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao); // bmp is bitmap from user image file
        bmp.recycle();
        byte[] byteArray = bao.toByteArray();

        return byteArray;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG,"Photo picker complete.");
        try {
            switch (requestCode) {

                case GET_FROM_GALLERY:
                    if (resultCode == Activity.RESULT_OK) {
                        Log.i(TAG,"Uploading image.");

                        //data gives you the image uri. Try to convert that to bitmap
                        //convert data to bitmap, display in ImageView
                        Uri imageUri = data.getData();
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        ImageView tv1;
                        tv1= (ImageView) findViewById(R.id.profilePictureDisplay);
                        tv1.setImageBitmap(bmp);

                        updateButtonText();

                        uploadBitmapAsProfilePicture(bmp,uid);

                        uploadedOwnPicture = true;

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

    public void uploadBitmapAsProfilePicture(Bitmap bitmap, final String uid){

        // Create a reference to profile picture
        StorageReference pfpRef = storageReference.child("users/"+uid+"/profile.png");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
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

                ref.child("users").child(uid).child("profile_pic_format").setValue("png");
                ref.child("users").child(uid).child("email").setValue(email);
                ref.child("users").child(uid).child("name").setValue(name);
                ref.child("users").child(uid).child("age").setValue(Integer.valueOf(mAge.getText().toString()));

            }
        });

    }

    public void updateButtonText() {
        // update button text
        Button uploadButton = findViewById(R.id.uploadPfpButton);
        uploadButton.setText("Select new profile picture");

        Button continueButton = findViewById(R.id.continueButton);
        continueButton.setText("Continue");

    }
}
