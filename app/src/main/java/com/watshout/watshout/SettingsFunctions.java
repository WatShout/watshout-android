package com.watshout.watshout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    public void watch() {

        viewHolder.individualSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "This feature is coming soon!", Toast.LENGTH_SHORT)
                        .show();
            }
        });

    }

    public void email() {

        //TODO: Open popup window that asks for new email
        //thisUser.updateEmail("testtesttest@user.com");

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
            if (user.getProviderId().equals("google.com")){
                hasGoogleProvider = true;
            }
        }

        return hasGoogleProvider;
    }

    public void password() {

        //TODO: Open popup asking for password
        //thisUser.updatePassword();

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
