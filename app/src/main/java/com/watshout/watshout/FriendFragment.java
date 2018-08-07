package com.watshout.watshout;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;

public class FriendFragment extends android.app.Fragment implements SwipeRefreshLayout.OnRefreshListener{

    RecyclerView mFriendRecyclerView;
    RecyclerView.Adapter friendAdapter;
    SwipeRefreshLayout mSwipeRefreshLayout;

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

        mFriendRecyclerView = view.findViewById(R.id.friendRecyclerView);
        mFriendRecyclerView.setHasFixedSize(true);
        mFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.friend_divider));

        mFriendRecyclerView.addItemDecoration(itemDecorator);

        friendAdapter = new FriendLoadingAdapter(0);
        mFriendRecyclerView.setAdapter(friendAdapter);

        // SwipeRefreshLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.friendSwipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);

        mSwipeRefreshLayout.setEnabled(false);
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

    public void getFriendRequests(final RecyclerView recyclerView) {

        mSwipeRefreshLayout.setRefreshing(true);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
        String url = EndpointURL.getInstance().getFriendRequestURL(uid);

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

                            RecyclerView.Adapter adapter =
                                    new FriendRequestAdapter(friendRequests, getActivity());
                            recyclerView.setAdapter(adapter);

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
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        String url = EndpointURL.getInstance().getFriendURL(uid);

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
        inflater.inflate(R.menu.friend_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {

            case R.id.open_requests:

                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.popup_friend_request, null);
                // create the popup window
                int width = LinearLayout.LayoutParams.MATCH_PARENT;
                int height = LinearLayout.LayoutParams.MATCH_PARENT;
                boolean focusable = true; // lets taps outside the popup also dismiss it

                PopupWindow popupWindow = new PopupWindow(popupView, width, height, focusable);
                popupWindow.setAnimationStyle(R.style.popup_window_animation);
                popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);

                RelativeLayout relativeLayout = popupView.findViewById(R.id.request_relative_layout);

                relativeLayout.setAlpha(0.8F);

                RecyclerView mRequestRecyclerView = popupView.findViewById(R.id.friendRequestView);

                getFriendRequests(mRequestRecyclerView);

                break;

            case R.id.send_request:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Enter Friend Email");

                // Set up the input
                final EditText input = new EditText(getActivity());
                // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
                input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        // Spinning dialog when adding friend
                        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
                        progressDialog.setMessage("Adding friend...");

                        sendRequest(input.getText().toString(), progressDialog);

                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    public void sendRequest(String theirEmail, final ProgressDialog progressDialog) {

        progressDialog.show();

        ref.child("users").orderByChild("email").equalTo(theirEmail)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if (dataSnapshot.getValue() != null){

                            for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {

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
                                                progressDialog.dismiss();


                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });

                            }



                        } else {

                            Toast.makeText(getActivity(), "User not found",
                                    Toast.LENGTH_SHORT).show();
                            progressDialog.dismiss();

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.d("EMAIL", databaseError.toString());
                        progressDialog.dismiss();
                    }
                });


    }

}