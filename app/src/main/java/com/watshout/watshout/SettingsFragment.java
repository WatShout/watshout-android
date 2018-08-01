package com.watshout.watshout;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.List;


public class SettingsFragment extends android.app.Fragment {

    ExpandableListView expandableListView;


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

        List<ParentItem> itemList = new ArrayList<ParentItem>();

        ParentItem parent1 = new ParentItem();
        parent1.getChildItemList().add(new ChildItem());
        parent1.getChildItemList().add(new ChildItem());
        parent1.getChildItemList().add(new ChildItem());

        ParentItem parent2 = new ParentItem();
        parent2.getChildItemList().add(new ChildItem());
        parent2.getChildItemList().add(new ChildItem());
        parent2.getChildItemList().add(new ChildItem());

        itemList.add(parent1);
        itemList.add(parent2);

        SettingsListAdapter adapter = new SettingsListAdapter(getActivity(), itemList);

        expandableListView.setAdapter(adapter);

    }

}