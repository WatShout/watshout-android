package com.watshout.watshout;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
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
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class InitializeNewAccountActivity extends AppCompatActivity {

    final long TEN_MEGABYTE = 10 * 1024 * 1024;

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    final String uid = thisUser.getUid();
    final String email = thisUser.getEmail();
    final String name = thisUser.getDisplayName();

    final static int PICK_IMAGE_CODE = 1;

    public static final int GET_FROM_GALLERY = 3;
    public static final String TAG = "PfpRetrieval";
    public final Context context = this;
    ImageView mProfile;
    boolean uploadedOwnPicture;

    boolean pickedBirthday = false;

    TextView mFirst;
    TextView mLast;
    TextView mBirthdayText;
    TextView mGender;
    TextView mEmail;
    TextView mTheirName;

    ProgressDialog progressDialog;

    Button mSave;

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageReference = storage.getReference();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    private DatePickerDialog.OnDateSetListener mDateSetListener;
    String birthday;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_initialize_account_new);

        mProfile = findViewById(R.id.myProfilePic);

        progressDialog = new ProgressDialog(InitializeNewAccountActivity.this);

        Picasso.get()
                .load(R.drawable.blank_profile)
                .resize(256, 256)
                .transform(new CircleTransform())
                .into(mProfile);

        uploadedOwnPicture = false;

        mSave = findViewById(R.id.mSave);

        mFirst = findViewById(R.id.firstname);
        mLast = findViewById(R.id.lastname);
        mBirthdayText = findViewById(R.id.birthday);
        mGender = findViewById(R.id.gender);
        mEmail = findViewById(R.id.email);
        mTheirName = findViewById(R.id.theirName);

        String[] names = name.split(" ");
        String firstName = names[0];
        String lastName = "";
        for (int i = 1; i < names.length; i++){
            lastName += names[i] + " ";
        }

        mFirst.setText(firstName);
        mLast.setText(lastName);
        mEmail.setText(email);
        mTheirName.setText(name);

        Calendar cal = Calendar.getInstance();

        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        final int day = cal.get(Calendar.DAY_OF_MONTH);

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(pickPhoto , PICK_IMAGE_CODE);//one can be replaced with any action code

            }
        });

        mGender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder b = new AlertDialog.Builder(InitializeNewAccountActivity.this);
                b.setTitle("Pick your gender");
                String[] types = {"Male", "Female", "Other"};
                b.setItems(types, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        switch(which){
                            case 0:
                                mGender.setText("Male");
                                break;
                            case 1:
                                mGender.setText("Female");
                                break;
                            case 2:
                                mGender.setText("Other");
                                break;
                        }
                    }

                });

                b.show();

            }
        });

        // Load current profile picture, display in ImageView
        ref.child("users").child(uid).child("profile_pic_format").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.getValue() != null){
                    String fileFormat = dataSnapshot.getValue().toString();
                    String fileName = "profile." + fileFormat;
                    uploadedOwnPicture = true;

                    storageReference.child("users").child(uid).child(fileName)
                            .getDownloadUrl()
                            .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {

                                    Picasso.get()
                                            .load(uri)
                                            .placeholder(R.drawable.loading)
                                            .resize(256, 256)
                                            .transform(new CircleTransform())
                                            .into(mProfile);

                                }
                            });

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mBirthdayText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatePickerDialog dialog = new DatePickerDialog(
                        InitializeNewAccountActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        mDateSetListener,
                        year, month, day);

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

            }
        });



        mDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                pickedBirthday = true;

                SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
                Date d = new Date(year - 1900, month, dayOfMonth);
                String strDate = dateFormatter.format(d);

                birthday = strDate;

                mBirthdayText.setText(strDate);

            }
        };

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                continueToMainActivity();
            }
        });

    }

    public void selectProfilePicture(View v){

        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent, GET_FROM_GALLERY);

    }

    public void continueToMainActivity(){

        boolean anyErrors = false;

        if (mGender.getText().toString().equals("Please select")){
            mGender.setTextColor(Color.parseColor("#FF0000"));
            anyErrors = true;
        }

        if (mBirthdayText.getText().toString().equals("Please select")){
            mBirthdayText.setTextColor(Color.parseColor("#FF0000"));
            anyErrors = true;
        }

        if (anyErrors){
            Toast.makeText(this, "Please fill out all required information",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ref.child("users").child(uid).child("profile_pic_format").setValue("png");
        ref.child("users").child(uid).child("email").setValue(email);
        ref.child("users").child(uid).child("name").setValue(name);
        ref.child("users").child(uid).child("birthday").setValue(birthday);

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

                    goToMainActivity();

                }
            });

        } else {
            Log.d("TEST", "This happens");
            goToMainActivity();
        }
    }

    public void goToMainActivity(){

        Carrier.setUploadedOwnProfilePicture(true);

        Intent openMain = new Intent(getApplicationContext(), MainActivity.class);
        openMain.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(openMain);
        finish();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){

        super.onActivityResult(requestCode, resultCode, data);

        Log.i(TAG,"Photo picker complete.");
        try {
            switch (requestCode) {

                case PICK_IMAGE_CODE:
                    if (resultCode == Activity.RESULT_OK) {
                        Log.i(TAG,"Uploading image.");

                        progressDialog.setMessage("Uploading profile picture...");
                        progressDialog.show();

                        //data gives you the image uri. Try to convert that to bitmap
                        //convert data to bitmap, display in ImageView
                        Uri imageUri = data.getData();
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                        bmp = ImageUtils.reshapeAsProfilePicture(bmp,this);

                        uploadBitmapAsProfilePicture(bmp,uid);

                        uploadedOwnPicture = true;
                        //fixScreen();

                        break;
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        Log.e(TAG, "Selecting picture cancelled");
                    }
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception in onActivityResult: " + e.getMessage());
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

                Log.d("PHOTO", "Upload worked");

                pfpRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {

                        progressDialog.dismiss();

                        Picasso.get()
                                .load(uri)
                                .resize(256, 256)
                                .transform(new CircleTransform())
                                .into(mProfile);

                    }
                });
            }
        });

    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(InitializeNewAccountActivity.this,SignInActivity.class);
        startActivity(intent);
        finish();
    }
}
