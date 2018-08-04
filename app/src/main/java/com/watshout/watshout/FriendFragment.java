package com.watshout.watshout;


import android.app.ActionBar;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.android.volley.Cache;
import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import xyz.danoz.recyclerviewfastscroller.vertical.VerticalRecyclerViewFastScroller;

import static android.content.Context.INPUT_METHOD_SERVICE;
import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class FriendFragment extends android.app.Fragment implements SwipeRefreshLayout.OnRefreshListener{

    RecyclerView mFriendRecyclerView;
    //RecyclerView mRequestRecyclerView;
    RecyclerView.Adapter friendAdapter;
    //RecyclerView.Adapter requestAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    //Button mSendRequest;
    //EditText mEmail;
    RelativeLayout mFriendRequestLayout;

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String email = thisUser.getEmail();
    String uid = thisUser.getUid();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        getActivity().setTitle("friends");

        //mFriendRequestLayout = view.findViewById(R.id.friend_request_layout);

        mFriendRecyclerView = view.findViewById(R.id.friendRecyclerView);
        mFriendRecyclerView.setHasFixedSize(true);
        mFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.friend_divider));

        mFriendRecyclerView.addItemDecoration(itemDecorator);

        //VerticalRecyclerViewFastScroller fastScroller = view.findViewById(R.id.fast_scroller);
        //fastScroller.setRecyclerView(mFriendRecyclerView);
        //mFriendRecyclerView.setOnScrollListener(fastScroller.getOnScrollListener());


        friendAdapter = new FriendLoadingAdapter(0);
        mFriendRecyclerView.setAdapter(friendAdapter);


        //mRequestRecyclerView = view.findViewById(R.id.friendRequestView);
        //mRequestRecyclerView.setHasFixedSize(true);
        //mRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.friendSwipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mSwipeRefreshLayout.setEnabled(false);

        /*
        mFriendRequestLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_friend_request, null);
                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it

                PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
            }
        }); */

        //mSendRequest = view.findViewById(R.id.sendRequest);
        //mEmail = view.findViewById(R.id.emailInput);
        /*

        mSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                final String theirEmail = mEmail.getText().toString();
                mEmail.setText("");

                ref.child("users").orderByChild("email").equalTo(theirEmail)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (dataSnapshot.getValue() != null){

                                    for (DataSnapshot childSnapshot: dataSnapshot.getChildren()) {

                                        final String theirUID = childSnapshot.getKey();

                                        ref.child("friend_data").child(uid).child(theirUID)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {

                                                        if (dataSnapshot.getValue() != null){

                                                            Toast.makeText(getActivity(),
                                                                    "You are already friends with this user",
                                                                    Toast.LENGTH_SHORT).show();

                                                        } else {

                                                            Log.d("EMAIL", uid + ", " + theirUID);

                                                            ref.child("friend_requests").child(uid).child(theirUID)
                                                                    .child("request_type").setValue("sent");

                                                            ref.child("friend_requests").child(theirUID).child(uid)
                                                                    .child("request_type").setValue("received");

                                                            Toast.makeText(getActivity(), "Request sent!", Toast.LENGTH_SHORT).show();

                                                        }


                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });

                                    }



                                } else {

                                    Toast.makeText(getActivity(), "User not found",
                                            Toast.LENGTH_SHORT).show();

                                }

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.d("EMAIL", databaseError.toString());
                            }
                        });


            }
        });

        */

        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                // Fetching data from server
                refreshData();
            }
        });

        ref.child("friend_data").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) { refreshData(); }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                refreshData();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        ref.child("friend_requests").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                refreshData();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                refreshData();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });



    }

    public void getFriendRequests() {

        mSwipeRefreshLayout.setRefreshing(true);

        Log.d("REQUEST_DATA", "hello");

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url ="https://watshout.herokuapp.com/friend_requests/" + uid + "/";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        ArrayList<FriendItem> friendRequests = new ArrayList<>();

                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("friend_requests");

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);

                                friendRequests.add(new FriendItem(
                                        o.getString("name"),
                                        o.getString("uid"),
                                        o.getString("profile_pic")
                                ));

                            }

                            Log.d("REQUEST_DATA", friendRequests.toString());

                            //ViewGroup.LayoutParams params= mRequestRecyclerView.getLayoutParams();
                            //params.height=50;
                            //mRequestRecyclerView.setLayoutParams(params);

                            //requestAdapter = new FriendRequestAdapter(friendRequests, getActivity());
                            //mRequestRecyclerView.setAdapter(requestAdapter);

                            mSwipeRefreshLayout.setRefreshing(false);

                        } catch (JSONException e) {

                            Log.d("REQUEST_DATA", "ERROR!!!");
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("REQUEST_DATA", error.toString());
            }

        });

        queue.add(stringRequest);

    }

    public void getFriendsList() {

        mSwipeRefreshLayout.setRefreshing(true);

        try {
            File cache = getActivity().getCacheDir();
        } catch (NullPointerException e){
            e.printStackTrace();
        }


        // Instantiate the RequestQueue.
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //RequestQueue requestQueue;
        //Cache cache = new DiskBasedCache(getActivity().getCacheDir(), 10 * 1024 * 1024); // 10MB cap
        //Network network = new BasicNetwork(new HurlStack());
        //requestQueue = new RequestQueue(cache, network);

        String url ="https://watshout.herokuapp.com/friends/" + uid + "/";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        mSwipeRefreshLayout.setRefreshing(false);

                        ArrayList<FriendItem> listItems = new ArrayList<>();

                        try {

                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray array = jsonObject.getJSONArray("friends");

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject o = array.getJSONObject(i);

                                listItems.add(new FriendItem(
                                        o.getString("name"),
                                        o.getString("uid"),
                                        o.getString("profile_pic")
                                ));

                            }

                            // Sort friends list in alphabetical order
                            Collections.sort(listItems);

                            friendAdapter = new FriendAdapter(listItems, getActivity());

                            SwipeController swipeController = new SwipeController(listItems,
                                    uid, getActivity(), friendAdapter, mFriendRecyclerView);

                            mFriendRecyclerView.setAdapter(friendAdapter);

                            ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
                            itemTouchhelper.attachToRecyclerView(mFriendRecyclerView);

                            mSwipeRefreshLayout.setRefreshing(false);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("ERROR", error.toString());
            }

        });

        requestQueue.add(stringRequest);

    }

    public void refreshData() {
        getFriendsList();
        //getFriendRequests();
    }

    @Override
    public void onRefresh() {
        getFriendsList();
        //getFriendRequests();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        super.onCreateOptionsMenu(menu, inflater);
    }

}