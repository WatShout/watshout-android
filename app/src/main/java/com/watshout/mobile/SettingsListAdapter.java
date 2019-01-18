package com.watshout.mobile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class SettingsListAdapter extends BaseExpandableListAdapter {

    private final List<ParentItem> itemList;
    private final LayoutInflater inflater;
    private Context context;
    private HashMap<Integer, String> settingsGroupNames;
    private String[][] settingsLabels;
    private SettingsFunctions settingsFunctions;

    SettingsListAdapter(Context context, List<ParentItem> itemList) {
        this.itemList = itemList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);

        settingsGroupNames = new HashMap<>();
        //settingsGroupNames.put(0, "Activity");
        //settingsGroupNames.put(0, "Setup");
        settingsGroupNames.put(0, "Account");
        //settingsGroupNames.put(2, "Watch");
        settingsGroupNames.put(1, "Legal");

        settingsLabels = new String[5][4];
        //settingsLabels[0][0] = "Data screens";
        //settingsLabels[0][0] = "Message presets";

        //settingsLabels[1][0] = "Units";
        //settingsLabels[1][0] = "Language";
        //settingsLabels[0][0] = "Permissions";

        settingsLabels[0][0] = "Email";
        settingsLabels[0][1] = "Password";
        //settingsLabels[2][2] = "Linked accounts";

        //settingsLabels[3][0] = "Pair a device";

        //settingsLabels[2][0] = "Support";
        settingsLabels[1][0] = "Privacy policy";
        //settingsLabels[2][2] = "License";
        settingsLabels[1][1] = "Terms of use";

        //

    }


    @Override
    public ChildItem getChild(int groupPosition, int childPosition) {

        return itemList.get(groupPosition).getChildItemList().get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return itemList.get(groupPosition).getChildItemList().size();
    }

    @Override
    public ParentItem getGroup(int groupPosition) {
        return itemList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return itemList.size();
    }

    @Override
    public long getGroupId(final int groupPosition) {
        return groupPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    static class GroupViewHolder {
        TextView mLabel;
        TextView mDescription;
        ImageView mIcon;
        ImageView mArrow;
        LinearLayout mLinearLayout;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        GroupViewHolder viewHolder;

        convertView = inflater.inflate(R.layout.settings_group, parent, false);

        viewHolder = new GroupViewHolder();
        viewHolder.mLabel = convertView.findViewById(R.id.setting_group_label);
        viewHolder.mDescription = convertView.findViewById(R.id.description);
        viewHolder.mIcon = convertView.findViewById(R.id.settingsIcon);
        viewHolder.mLinearLayout = convertView.findViewById(R.id.settingGroup);
        viewHolder.mArrow = convertView.findViewById(R.id.help_group_indicator);

        if (isExpanded){
            viewHolder.mArrow.setImageResource(R.drawable.arrow_down);
            viewHolder.mLabel.setTextColor(Color.parseColor("#ffd529"));
        } else {
            viewHolder.mArrow.setImageResource(R.drawable.arrow_left);
            viewHolder.mLabel.setTextColor(Color.parseColor("#ffffff"));
        }

        switch (groupPosition) {

            case 9:
                viewHolder.mIcon.setImageResource(R.drawable.running);
                break;
            case 99:
                viewHolder.mIcon.setImageResource(R.drawable.wrench);
                break;
            case 0:
                viewHolder.mIcon.setImageResource(R.drawable.person);
                break;
            case 9999:
                viewHolder.mIcon.setImageResource(R.drawable.instagram_watch);
                break;
            case 1:
                viewHolder.mIcon.setImageResource(R.drawable.legal);
                break;

        }

        viewHolder.mLabel.setText(settingsGroupNames.get(groupPosition));

        String description = "";

        for (int i = 0; i < settingsLabels[groupPosition].length; i++) {

            if (settingsLabels[groupPosition][i] != null){

                description += settingsLabels[groupPosition][i] + ", ";
            }

        }

        // Remove the ", " at the end of the string
        description = description.toLowerCase();
        //Log.d("TEST", description);
        description = description.substring(0, description.length() - 2);

        // Make the first character capital
        //String firstLetter = description.substring(0, 1).toUpperCase();
        //description = firstLetter + description.substring(1);

        viewHolder.mDescription.setText(description);

        Log.d("POSITION", groupPosition + "");

        convertView.setTag(viewHolder);

        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             final ViewGroup parent) {

        final SettingViewHolder viewHolder;

        convertView = inflater.inflate(R.layout.individual_setting, parent, false);

        viewHolder = new SettingViewHolder();
        viewHolder.individualSetting = convertView.findViewById(R.id.individual_setting);
        viewHolder.mLabel = convertView.findViewById(R.id.setting_name);
        viewHolder.mLabel.setText(settingsLabels[groupPosition][childPosition]);

        settingsFunctions = new SettingsFunctions(context, viewHolder, convertView);

        switch (settingsLabels[groupPosition][childPosition]) {

            case "Language":
                settingsFunctions.language();
                break;

            case "Units":
                settingsFunctions.units();
                break;

            case "Email":
                settingsFunctions.email();
                break;

            case "Password":
                settingsFunctions.password();
                break;

            case "Pair a device":
                settingsFunctions.watch();
                break;

            case "Support":
                settingsFunctions.support();
                break;

            case "Privacy policy":
                Intent privacyIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.watshout.com/privacy"));
                context.startActivity(privacyIntent);
                break;

            case "License":
                settingsFunctions.license();
                break;

            case "Terms of use":
                Intent termIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.watshout.com/terms"));
                context.startActivity(termIntent);
                //settingsFunctions.termsOfUse();
                break;

            case "Message presets":
                settingsFunctions.messagePresets();
                break;

            case "Linked accounts":
                settingsFunctions.connectStrava();
                break;

        }

        return convertView;
    }




}