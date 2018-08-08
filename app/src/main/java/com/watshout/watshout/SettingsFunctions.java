package com.watshout.watshout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import static android.app.Activity.RESULT_OK;

public class SettingsFunctions {

    // General database reference
    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();

    String uid = thisUser.getUid();
    String email = thisUser.getEmail();
    String name = thisUser.getDisplayName();

    private Context context;
    private SettingViewHolder viewHolder;
    private View convertView;


    SettingsFunctions(Context context, SettingViewHolder viewHolder, View convertView){
        this.context = context;
        this.viewHolder = viewHolder;
        this.convertView = convertView;
    }

    // TODO: Better redirect to main app
    public void permissions() {

        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                context.startActivity(i);
            }
        });

    }

    public void connectStrava() {


        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ref.child("users").child(uid).child("strava_token").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            Toast.makeText(context, "You are already authenticated with Strava!",
                                    Toast.LENGTH_SHORT).show();
                        } else {

                            Intent intent = new Intent(context, StravaAuthenticate.class);
                            context.startActivity(intent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });
    }


    public void messagePresets() {
        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Configure messages");

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                String storedFirstMessage = settings.getString("first_message",
                        "Need help. Can you come?");
                String storedSecondMessage = settings.getString("second_message",
                        "On my way back.");
                String storedThirdMessage = settings.getString("third_message",
                        "I'm safe.");

                // Set up the input
                final EditText firstMessage = new EditText(context);
                final EditText secondMessage = new EditText(context);
                final EditText thirdMessage = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                firstMessage.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                secondMessage.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                thirdMessage.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);

                firstMessage.setText(storedFirstMessage);
                secondMessage.setText(storedSecondMessage);
                thirdMessage.setText(storedThirdMessage);

                layout.addView(firstMessage);
                layout.addView(secondMessage);
                layout.addView(thirdMessage);

                builder.setView(layout);

                // Set up the buttons
                builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString("first_message", firstMessage.getText().toString());
                        editor.putString("second_message", secondMessage.getText().toString());
                        editor.putString("third_message", thirdMessage.getText().toString());
                        editor.apply();


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();

            }
        });
    }

    public void watch() {

        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "This feature is coming soon!", Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }

    public void support() {

        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Lol you're out of luck", Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }


    public void privacyPolicy() {

        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Who actually reads this?", Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }


    public void license() {

        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Are you the CIA?", Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }


    public void termsOfUse() {

        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Please go outside", Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }


    public void email() {

        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (containsGoogleProvider()){
                    Toast.makeText(context,
                            "Your watshout account is managed via your Google account\n" +
                            "Your credentials cannot be changed here",
                            Toast.LENGTH_LONG)
                            .show();

                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Set new email");

                // Set up the input
                final EditText input = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        final String newEmail = input.getText().toString();

                        boolean isValidEmail = android.util.Patterns.EMAIL_ADDRESS
                                .matcher(newEmail).matches();

                        if (isValidEmail) {
                            ref.child("users").child(uid).child("email").setValue(newEmail)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            thisUser.updateEmail(newEmail);
                                        }
                                    });
                        }

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });


    }

    private boolean containsGoogleProvider() {

        Boolean hasGoogleProvider = false;

        for (UserInfo user : FirebaseAuth.getInstance().getCurrentUser().getProviderData()) {
            if (user.getProviderId().equals("google.com")) {
                hasGoogleProvider = true;
            }
        }

        return hasGoogleProvider;
    }

    public void password() {

        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (containsGoogleProvider()){
                    Toast.makeText(context,
                            "Your watshout account is managed via your Google account\n" +
                                    "Your credentials cannot be changed here",
                            Toast.LENGTH_LONG)
                            .show();

                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Set new password");

                LinearLayout layout = new LinearLayout(context);
                layout.setOrientation(LinearLayout.VERTICAL);

                // Set up the input
                final EditText firstPassword = new EditText(context);
                final EditText secondPassword = new EditText(context);
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                firstPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
                secondPassword.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);

                layout.addView(firstPassword);
                layout.addView(secondPassword);

                builder.setView(layout);

                // Set up the buttons
                builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Boolean match = firstPassword.getText().toString()
                                .equals(secondPassword.getText().toString());

                        Boolean longEnough = firstPassword.length() >= 8;

                        if (match && longEnough) {
                            thisUser.updatePassword(firstPassword.getText().toString());
                        }


                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

    }

    public void language() {
        viewHolder.mValue = convertView.findViewById(R.id.settingValue);
        viewHolder.mValue.setText(getLanguage());
    }

    private String getLanguage() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString("Language", "English");
    }

    public void units() {
        viewHolder.mValue = convertView.findViewById(R.id.settingValue);
        viewHolder.mValue.setText(getUnits());

        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String current = viewHolder.mValue.getText().toString();

                String newUnits;

                if (current.equals("Imperial")) {
                    newUnits = "Metric";
                } else {
                    newUnits = "Imperial";
                }

                SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("Units", newUnits);
                editor.apply();

                viewHolder.mValue.setText(newUnits);

            }

        });
    }

    private String getUnits() {
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString("Units", "Imperial");
    }

}
