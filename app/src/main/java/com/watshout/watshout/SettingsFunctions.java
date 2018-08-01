package com.watshout.watshout;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

    public void email() {

        //TODO: Open popup window that asks for new email
        //thisUser.updateEmail("testtesttest@user.com");

    }

    public void password() {

        //TODO: Open popup asking for password
        //thisUser.updatePassword();

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
