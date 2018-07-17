package com.watshout.tracker;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.tasks.OnCompleteListener;
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

public class RequireProfilePictureActivity extends AppCompatActivity implements IPickResult {

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String uid = thisUser.getUid();

    public static final int GET_FROM_GALLERY = 3;
    public static final String TAG = "PfpRetrieval";
    public final Context context = this;
    ImageView mProfile;

    boolean uploadedOwnPicture = false;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_require_pfp);

        mProfile = findViewById(R.id.profilePictureDisplay);

    }

    public void selectProfilePicture(View v){

        PickImageDialog.build(new PickSetup()).show(RequireProfilePictureActivity.this);

    }

    public void continueToMainActivity(View v){

        if (!uploadedOwnPicture) {

            ProgressDialog nDialog;
            nDialog = new ProgressDialog(RequireProfilePictureActivity.this);
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
    public void onPickResult(PickResult pickResult) {

        Bitmap bmp1 = pickResult.getBitmap();
        Bitmap bmp2 = bmp1.copy(bmp1.getConfig(), true);

        mProfile.setImageBitmap(bmp1);

        final byte[] data = getImageData(bmp2);

        uploadedOwnPicture = true;
        uploadProfilePicture(data);

    }

    private void uploadProfilePicture(byte[] imageData){

        updateButtonText();

        storageReference.child("users/" + uid + "/profile.png")
                .putBytes(imageData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                ref.child("users").child(uid).child("profile_pic_format").setValue("png");


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
