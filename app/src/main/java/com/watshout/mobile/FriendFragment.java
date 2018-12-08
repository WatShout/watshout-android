package com.watshout.mobile;


import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.watshout.mobile.pojo.Friend;
import com.watshout.mobile.pojo.FriendObject;
import com.watshout.mobile.pojo.FriendRequest;
import com.watshout.mobile.pojo.FriendRequestList;
import com.watshout.mobile.pojo.FriendRequestResponse;
import com.watshout.mobile.pojo.FriendsList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;

public class FriendFragment extends android.app.Fragment {

    RecyclerView mFriendRecyclerView;
    RecyclerView.Adapter friendAdapter;

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String email = thisUser.getEmail();
    String uid = thisUser.getUid();

    RetrofitInterface retrofitInterface = RetrofitClient
            .getRetrofitInstance().create(RetrofitInterface.class);

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    Menu globalMenu;
    MenuInflater globalInflater;

    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        getActivity().setTitle("Friends");

        mFriendRecyclerView = view.findViewById(R.id.friendRecyclerView);
        mFriendRecyclerView.setHasFixedSize(true);
        mFriendRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        DividerItemDecoration itemDecorator = new DividerItemDecoration(getActivity(),
                DividerItemDecoration.VERTICAL);
        itemDecorator.setDrawable(Objects.requireNonNull(ContextCompat.getDrawable(getActivity(),
                R.drawable.friend_divider)));

        mFriendRecyclerView.addItemDecoration(itemDecorator);

        friendAdapter = new FriendLoadingAdapter(0);
        mFriendRecyclerView.setAdapter(friendAdapter);


        // This does an initial load and populates friends list
        ref.child("friend_data").child(uid).addListenerForSingleValueEvent(
            new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    getFriendsList();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) { }
            });
    }

    public void getFriendsList() {

        ProgressDialog pd = new ProgressDialog(getActivity());
        pd.setMessage("Loading Friends...");
        pd.setCancelable(false);
        pd.show();

        Call<FriendsList> call = retrofitInterface.getFriendsList(uid);

        call.enqueue(new Callback<FriendsList>() {
            @Override
            public void onResponse(Call<FriendsList> call, retrofit2.Response<FriendsList> response) {
                List<Friend> friendList = response.body().getFriends();

                Collections.sort(friendList);

                Call<FriendRequestList> friendRequestCall = retrofitInterface.getFriendRequestList(uid);

                friendRequestCall.enqueue(new Callback<FriendRequestList>() {
                    @Override
                    public void onResponse(Call<FriendRequestList> call, retrofit2.Response<FriendRequestList> response) {

                        List<FriendRequest> friendRequestList = response.body().getFriendRequests();

                        if (friendRequestList.size() > 0) {
                            globalMenu.clear();
                            globalInflater.inflate(R.menu.base_menu_requests, globalMenu);
                        }

                        List<FriendObject> everything = new ArrayList<>();
                        everything.addAll(friendList);
                        everything.addAll(friendRequestList);

                        friendAdapter = new FriendAdapter(everything, getActivity(), friendList.size());

                        SwipeController swipeController = new SwipeController((ArrayList<Friend>) friendList,
                                uid, getActivity(), (FriendAdapter) friendAdapter, mFriendRecyclerView);

                        mFriendRecyclerView.setAdapter(friendAdapter);

                        ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeController);
                        itemTouchhelper.attachToRecyclerView(mFriendRecyclerView);

                        pd.dismiss();

                    }

                    @Override
                    public void onFailure(Call<FriendRequestList> call, Throwable t) {

                    }
                });

            }

            @Override
            public void onFailure(Call<FriendsList> call, Throwable t) { }
        });
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.globalMenu = menu;
        this.globalInflater = inflater;
        menu.clear();
        inflater.inflate(R.menu.add_friend_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.send_request:
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                android.app.AlertDialog dialog;
                builder.setTitle("Search Friend by Email");

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

                dialog = builder.create();
                dialog.getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                builder.show();
                return super.onOptionsItemSelected(item);
        }
        return false;
    }

    public void firebaseFriendRequest(String theirUID){
        ref.child("friend_requests").child(uid).child(theirUID)
                .child("request_type").setValue("sent");

        ref.child("friend_requests").child(theirUID).child(uid)
                .child("request_type").setValue("received");
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

                                            firebaseFriendRequest(theirUID);

                                            Call<FriendRequestResponse> call =
                                                    retrofitInterface.sendFriendNotification(uid, theirUID);

                                            call.enqueue(new Callback<FriendRequestResponse>() {
                                                @Override
                                                public void onResponse(Call<FriendRequestResponse> call,
                                                                       retrofit2.Response<FriendRequestResponse> response) {

                                                    Toast.makeText(getActivity(),
                                                            "Friend Request Sent!",
                                                            Toast.LENGTH_SHORT).show();
                                                }

                                                @Override
                                                public void onFailure(Call<FriendRequestResponse> call, Throwable t) {

                                                }
                                            });
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