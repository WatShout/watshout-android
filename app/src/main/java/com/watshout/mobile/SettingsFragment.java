package com.watshout.mobile;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;


public class SettingsFragment extends android.app.Fragment {

    ExpandableListView expandableListView;
    private int lastPosition = -1;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        expandableListView = view.findViewById(R.id.listView);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("Settings");

        List<ParentItem> itemList = new ArrayList<ParentItem>();

        //ParentItem parent1 = new ParentItem();
        //parent1.getChildItemList().add(new ChildItem());
        //parent1.getChildItemList().add(new ChildItem());

        //ParentItem parent2 = new ParentItem();
        //parent2.getChildItemList().add(new ChildItem());
        //parent2.getChildItemList().add(new ChildItem());
        //parent2.getChildItemList().add(new ChildItem());

        ParentItem parent3 = new ParentItem();
        parent3.getChildItemList().add(new ChildItem());
        parent3.getChildItemList().add(new ChildItem());
        parent3.getChildItemList().add(new ChildItem());

        //ParentItem parent4 = new ParentItem();
        //parent4.getChildItemList().add(new ChildItem());

        ParentItem parent5 = new ParentItem();
        //parent5.getChildItemList().add(new ChildItem());
        parent5.getChildItemList().add(new ChildItem());
        //parent5.getChildItemList().add(new ChildItem());
        parent5.getChildItemList().add(new ChildItem());

        //itemList.add(parent1);
        //itemList.add(parent2);
        itemList.add(parent3);
        //itemList.add(parent4);
        itemList.add(parent5);

        SettingsListAdapter adapter = new SettingsListAdapter(getActivity(), itemList);

        Display newDisplay = getActivity().getWindowManager().getDefaultDisplay();
        int width = newDisplay.getWidth();
        expandableListView.setIndicatorBounds(width - 200, width);

        expandableListView.setAdapter(adapter);
        expandableListView.setOverscrollFooter(new ColorDrawable(Color.TRANSPARENT));
        expandableListView.setGroupIndicator(null);

        expandableListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {

                if (lastPosition != -1 && groupPosition != lastPosition) {
                    expandableListView.collapseGroup(lastPosition);
                }
                lastPosition = groupPosition;
            }
        });
    }
}

// Skeleton classes just used to populate the settings list
class ChildItem {}

class ParentItem {

    private List<ChildItem> childItemList;

    ParentItem() {
        childItemList = new ArrayList<ChildItem>();
    }

    public List<ChildItem> getChildItemList() {
        return childItemList;
    }

}