package com.watshout.watshout;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class SettingsListAdapter extends BaseExpandableListAdapter {

    private final List<ParentItem> itemList;
    private final LayoutInflater inflater;
    private Context context;
    private HashMap<Integer, String> settingsGroupNames;
    private String[][] settingsLabels;

    public SettingsListAdapter(Context context, List<ParentItem> itemList) {
        this.itemList = itemList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);

        settingsGroupNames = new HashMap<>();
        settingsGroupNames.put(0, "Activity");
        settingsGroupNames.put(1, "Setup");
        settingsGroupNames.put(2, "Account");
        settingsGroupNames.put(3, "Watch");
        settingsGroupNames.put(4, "Legal");

        settingsLabels = new String[5][4];
        settingsLabels[0][0] = "Data screens";
        settingsLabels[0][1] = "Message presets";

        settingsLabels[1][0] = "Units";
        settingsLabels[1][1] = "Language";
        settingsLabels[1][2] = "Permissions";

        settingsLabels[2][0] = "Email";
        settingsLabels[2][1] = "Password";
        settingsLabels[2][2] = "Linked accounts";

        settingsLabels[3][0] = "Pair a device";

        settingsLabels[4][0] = "Support";
        settingsLabels[4][1] = "Privacy policy";
        settingsLabels[4][2] = "License";
        settingsLabels[4][3] = "Terms of use";

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
        TextView mTest;
    }

    static class SettingViewHolder {
        TextView mTest;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        GroupViewHolder viewHolder;

        if (convertView == null) {

            convertView = inflater.inflate(R.layout.settings_group, parent, false);

            viewHolder = new GroupViewHolder();
            viewHolder.mTest = convertView.findViewById(R.id.setting_group_label);

            viewHolder.mTest.setText(settingsGroupNames.get(groupPosition));

            Log.d("POSITION", groupPosition + "");

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (GroupViewHolder) convertView.getTag();
        }

        return convertView;

    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             final ViewGroup parent) {

        GroupViewHolder viewHolder;

        convertView = inflater.inflate(R.layout.individual_setting, parent, false);

        viewHolder = new GroupViewHolder();
        viewHolder.mTest = convertView.findViewById(R.id.setting_name);

        viewHolder.mTest.setText(settingsLabels[groupPosition][childPosition]);

        convertView.setTag(viewHolder);


        return convertView;
    }


}