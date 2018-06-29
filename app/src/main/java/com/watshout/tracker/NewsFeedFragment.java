package com.watshout.tracker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class NewsFeedFragment extends android.app.Fragment {

    HashMap<String, List<String>> hashMap;
    List<String> stringList;
    ExpandableListView mExpandableListView;
    NewsFeedAdapter newsFeedAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news_feed, container, false);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mExpandableListView = view.findViewById(R.id.exp_list_view);
        hashMap = new HashMap<>();

        List<String> one = new ArrayList<String>();
        one.add("ONE");
        one.add("FIRST");
        one.add("HELLO!");

        List<String> two = new ArrayList<String>();
        two.add("two");
        two.add("SECOND");
        two.add("TEST");


        List<String> three = new ArrayList<String>();
        three.add("3333");
        three.add("third");
        three.add("litty");

        hashMap.put("ONE", one);
        hashMap.put("TWO", two);
        hashMap.put("THREE", three);

        stringList = new ArrayList<>(hashMap.keySet());

        newsFeedAdapter = new NewsFeedAdapter(getActivity(), hashMap, stringList);
        mExpandableListView.setAdapter(newsFeedAdapter);



    }

}
