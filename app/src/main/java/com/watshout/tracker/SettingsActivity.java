package com.watshout.tracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

public class SettingsActivity extends AppCompatActivity {

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();

    String uid = thisUser.getUid();
    String email = thisUser.getEmail();
    String name = thisUser.getDisplayName();

    FirebaseStorage storage = FirebaseStorage.getInstance();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();


    ImageView mProfile;
    EditText mEmail;
    EditText mAge;
    Button mEmailButton;
    Button mAgeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mProfile = findViewById(R.id.profile);
        mEmail = findViewById(R.id.email);
        mAge = findViewById(R.id.age);
        mEmailButton = findViewById(R.id.email_button);
        mAgeButton = findViewById(R.id.age_button);

        mAgeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int newAge = Integer.parseInt(mAge.getText().toString());




            }
        });



    }
}
