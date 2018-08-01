package com.watshout.watshout;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.List;

public class SettingsListAdapter extends BaseExpandableListAdapter {

    private final List<ParentItem> itemList;
    private final LayoutInflater inflater;
    private Context context;

    public SettingsListAdapter(Context context, List<ParentItem> itemList) {
        this.itemList = itemList;
        this.context = context;
        this.inflater = LayoutInflater.from(context);
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

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        View resultView = convertView;

        if (resultView == null) {

            resultView = inflater.inflate(R.layout.test_item, null); //TODO change layout id

        }


        return resultView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             final ViewGroup parent) {

        View resultView = convertView;

        if (resultView == null) {

            resultView = inflater.inflate(R.layout.other_test_item, null); //TODO change layout id

        }


        return resultView;
    }


}