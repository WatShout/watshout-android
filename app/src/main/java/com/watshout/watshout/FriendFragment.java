package com.watshout.watshout;


import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
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

import java.util.ArrayList;

public class FriendFragment extends android.app.Fragment implements SwipeRefreshLayout.OnRefreshListener{

    RecyclerView mFriendRecyclerView;
    RecyclerView mRequestRecyclerView;
    RecyclerView.Adapter friendAdapter;
    RecyclerView.Adapter requestAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;
    Button mSendRequest;
    EditText mEmail;

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

        mFriendRecyclerView = view.findViewById(R.id.friendRecyclerView);
        mFriendRecyclerView.setHasFixedSize(true);
        mFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mRequestRecyclerView = view.findViewById(R.id.friendRequestView);
        mRequestRecyclerView.setHasFixedSize(true);
        mRequestRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.friendSwipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mSendRequest = view.findViewById(R.id.sendRequest);
        mEmail = view.findViewById(R.id.emailInput);

        mSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final String theirEmail = mEmail.getText().toString();

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

        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                // Fetching data from server
                getFriendsList();

                getFriendRequests();
            }
        });

        ref.child("friend_data").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                onRefresh();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                onRefresh();
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });

        ref.child("friend_requests").child(uid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                onRefresh();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {}

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                onRefresh();
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

                            requestAdapter = new FriendRequestAdapter(friendRequests, getActivity());
                            mRequestRecyclerView.setAdapter(requestAdapter);

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

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
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

                            Log.d("FRIEND_DATA", listItems.toString());

                            friendAdapter = new FriendAdapter(listItems, getActivity());
                            mFriendRecyclerView.setAdapter(friendAdapter);

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

        queue.add(stringRequest);

    }

    @Override
    public void onRefresh() {
        getFriendsList();
        getFriendRequests();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu items for use in the action bar
        inflater.inflate(R.menu.friend, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}