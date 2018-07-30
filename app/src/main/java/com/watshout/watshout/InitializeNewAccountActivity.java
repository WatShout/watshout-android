package com.watshout.watshout;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.vansuita.pickimage.bean.PickResult;
import com.vansuita.pickimage.bundle.PickSetup;
import com.vansuita.pickimage.dialog.PickImageDialog;
import com.vansuita.pickimage.listeners.IPickResult;

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

    public static final int GET_FROM_GALLERY = 3;
    public static final String TAG = "PfpRetrieval";
    public final Context context = this;
    ImageView mProfile;
    EditText mAge;

    Button mBirthday;
    boolean uploadedOwnPicture;

    boolean pickedBirthday = false;

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
        setContentView(R.layout.activity_initialize_account);

        uploadedOwnPicture = Carrier.getUploadedOwnProfilePicture();

        mProfile = findViewById(R.id.profilePictureDisplay);
        mAge = findViewById(R.id.age);
        mBirthday = findViewById(R.id.birthdayButton);

        Calendar cal = Calendar.getInstance();

        final int year = cal.get(Calendar.YEAR);
        final int month = cal.get(Calendar.MONTH);
        final int day = cal.get(Calendar.DAY_OF_MONTH);

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
                                            .centerCrop()
                                            .into(mProfile);

                                }
                            });

                    /*
                    storageReference.child("users").child(uid).child(fileName).getBytes(TEN_MEGABYTE)
                            .addOnSuccessListener(new OnSuccessListener<byte[]>() {
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
                    */

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mBirthday.setOnClickListener(new View.OnClickListener() {
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

                mBirthday.setText(strDate);

            }
        };

        fixScreen();

    }

    public void fixScreen(){
        TextView title = findViewById(R.id.addPfp);

        Log.d("Debug",uploadedOwnPicture+"");

        if (uploadedOwnPicture){
            // update title
            title.setText("Change Profile Picture");

            // resize ImageView
            ImageView tv1;
            tv1= (ImageView) findViewById(R.id.profilePictureDisplay);

            final int numDp = 250;
            Resources r = getResources();
            final int pxDim = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, numDp, r.getDisplayMetrics());

            Log.d("Debug","Profile picture side length: "+pxDim+"px");

            tv1.getLayoutParams().width = pxDim;
            tv1.getLayoutParams().height = pxDim;

            // Conceal date picker
            ((Button)findViewById(R.id.birthdayButton)).setVisibility(View.INVISIBLE);
        }
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
                    ref.child("users").child(uid).child("birthday").setValue(birthday);

                    goToMainActivity();

                }
            });

            uploadedOwnPicture = true;
        } else {
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
                        bmp = ImageUtils.reshapeAsProfilePicture(bmp,this);

                        // display selected image
                        ImageView tv1;
                        tv1= (ImageView) findViewById(R.id.profilePictureDisplay);
                        tv1.setImageBitmap(bmp);

                        // resize ImageView
                        final int numDp = 250;
                        Resources r = getResources();
                        final int pxDim = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, numDp, r.getDisplayMetrics());

                        Log.d("Debug","Profile picture side length: "+pxDim+"px");

                        tv1.getLayoutParams().width = pxDim;
                        tv1.getLayoutParams().height = pxDim;

                        updateButtonText();
                        uploadBitmapAsProfilePicture(bmp,uid);

                        uploadedOwnPicture = true;
                        fixScreen();

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
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                ref.child("users").child(uid).child("profile_pic_format").setValue("png");
                ref.child("users").child(uid).child("email").setValue(email);
                ref.child("users").child(uid).child("name").setValue(name);
                ref.child("users").child(uid).child("birthday").setValue(birthday);

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
