package com.watshout.watshout;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.vansuita.pickimage.listeners.IPickCancel;
import com.vansuita.pickimage.listeners.IPickResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;

import pl.aprilapps.easyphotopicker.DefaultCallback;
import pl.aprilapps.easyphotopicker.EasyImage;

public class SettingsFragment extends android.app.Fragment {
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
    String[] permissions = {android.Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE};

    static final String TAG = "ChangePfp";

    final int GET_FROM_GALLERY = 123;

    Context mContext;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        super.onCreateView(inflater,container,savedInstanceState);

        mContext = getActivity();

        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("settings");

        mProfile = view.findViewById(R.id.profile);
        mEmail = view.findViewById(R.id.email);
        mAge = view.findViewById(R.id.age);
        mEmailButton = view.findViewById(R.id.email_button);
        mAgeButton = view.findViewById(R.id.age_button);

        mProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.i(TAG,"Opening photo picker.");

                Intent openPfp = new Intent(getActivity().getApplicationContext(), InitializeNewAccountActivity.class);
                openPfp.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().getApplicationContext().startActivity(openPfp);

                /*Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GET_FROM_GALLERY);*/

                //EasyImage.openChooserWithGallery(SettingsFragment.this, "Choose Profile Picture", 0);

            }
        });

        checkCameraPermissions();
        Log.i(TAG,"Checked camera permissions.");


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

        /*mAgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mAge.getText().toString().length() > 0){

                    int newAge = Integer.parseInt(mAge.getText().toString());
                    ref.child("users").child(uid).child("age").setValue(newAge);

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                    mAge.clearFocus();
                    mAge.setCursorVisible(false);

                }
            }
        });
        */
    }

    byte[] getImageData(Bitmap bmp) {

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, bao); // bmp is bitmap from user image file
        bmp.recycle();
        byte[] byteArray = bao.toByteArray();

        return byteArray;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode,resultCode,data);

        Log.i(TAG,"Profile picture selected!");

        /*EasyImage.handleActivityResult(requestCode, resultCode, data, this.getActivity(), new DefaultCallback() {
            @Override
            public void onImagePickerError(Exception e, EasyImage.ImageSource source, int type) {
                //Some error handling
                Log.i(TAG,"Error when selecting images.");
            }

            @Override
            public void onImagesPicked(List<File> imagesFiles, EasyImage.ImageSource source, int type) {
                //Handle the images
                Log.i(TAG,"Received "+imagesFiles.size()+" images");

                String filePath = imagesFiles.get(0).getPath();
                Bitmap bitmap = BitmapFactory.decodeFile(filePath);

                Bitmap bitmapCopy = bitmap.copy(bitmap.getConfig(), true);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                bitmapCopy.compress(Bitmap.CompressFormat.PNG, 100, baos);
                final byte[] pictureData = baos.toByteArray();

                if (fileName != null){
                    storageReference.child("users").child(uid).child(fileName)
                            .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            storageReference.child("users").child(uid).child("profile.png")
                                    .putBytes(pictureData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                            .putBytes(pictureData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
            }
        });*/

        try {

            switch (requestCode){

                case GET_FROM_GALLERY:
                    /*Uri imageUri = data.getData();
                    final Bitmap bmp1 = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                    final Bitmap bmp2 = bmp1.copy(bmp1.getConfig(), true);

                    mProfile.setImageBitmap(bmp2);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bmp1.compress(Bitmap.CompressFormat.PNG, 100, baos);
                    final byte[] pictureData = baos.toByteArray();

                    if (fileName != null){
                        storageReference.child("users").child(uid).child(fileName)
                                .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                storageReference.child("users").child(uid).child("profile.png")
                                        .putBytes(pictureData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                                .putBytes(pictureData).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
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
                    }*/

                    if (resultCode == Activity.RESULT_OK) {
                        //data gives you the image uri. Try to convert that to bitmap
                        //convert data to bitmap, display in ImageView
                        Uri imageUri = data.getData();
                        Bitmap bmp = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), imageUri);
                        mProfile.setImageBitmap(bmp);

                        uploadBitmapAsProfilePicture(bmp,uid);

                        break;
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        Log.e(TAG, "Selecting picture cancelled");
                    }
                    break;

            }


        } catch (IOException e){
            Log.e(TAG, "Exception in onActivityResult : " + e.getMessage());
        }

    }

    public void checkCameraPermissions() {
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this.getActivity(),
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            Log.e(TAG,"Some camera permissions not granted.");

            ActivityCompat.requestPermissions(this.getActivity(),
                    permissions,
                    MY_PERMISSIONS_REQUEST_CAMERA);


        } else Log.i(TAG,"Proceeding with required permissions.");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);

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
                Toast.makeText(getActivity(),"Failed to upload image",Toast.LENGTH_LONG);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                ref.child("users").child(uid).child("profile_pic_format").setValue("png");
                ref.child("users").child(uid).child("email").setValue(email);
                ref.child("users").child(uid).child("name").setValue(name);
                //ref.child("users").child(uid).child("age").setValue(Integer.valueOf(mAge.getText().toString()));

            }
        });

    }
}
