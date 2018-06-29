package com.watshout.tracker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;

public class NewsFeedAdapter extends BaseExpandableListAdapter {

    private Context context;
    private HashMap<String, List<String>> testData;
    private List<String> stringList;

    NewsFeedAdapter(Context context, HashMap<String, List<String>> testData, List<String> stringList){

        this.context = context;
        this.testData = testData;
        this.stringList = stringList;

    }

    @Override
    public int getGroupCount() {
        return testData.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        return testData.get(stringList.get(groupPosition)).size();

    }

    @Override
    public Object getGroup(int groupPosition) {
        return stringList.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        return testData.get(stringList.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {

        String groupTitle = (String) getGroup(groupPosition);

        if (convertView == null){
            LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(R.layout.news_feed_parent, parent, false);
        }

        TextView parentTextView = convertView.findViewById(R.id.parent_txt);
        parentTextView.setText(groupTitle);

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        String childTitle = (String) getChild(groupPosition, childPosition);

        if (convertView == null){

            LayoutInflater inflator = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(R.layout.news_feed_child, parent, false);

        }

        TextView childText = convertView.findViewById(R.id.child_txt);
        childText.setText(childTitle);

        return convertView;

    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }
}
