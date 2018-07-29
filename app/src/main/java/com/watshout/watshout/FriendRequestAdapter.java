package com.watshout.watshout;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private List<FriendItem> listItems;
    private Context context;

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String email = thisUser.getEmail();
    String uid = thisUser.getUid();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();


    FriendRequestAdapter(List<FriendItem> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_request_card, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final FriendItem friendItem = listItems.get(position);

        holder.mName.setText(friendItem.getName());
        loadProfilePic(friendItem.getProfilePic(), holder.mProfilePic);

        final String theirUID = friendItem.getUID();

        final String millis = Long.toString(System.currentTimeMillis());

        holder.mAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d("FRIEND", "Clicked accept");

                ref.child("friend_data").child(uid).child(theirUID).setValue(millis);
                ref.child("friend_data").child(theirUID).child(uid).setValue(millis);

                ref.child("friend_requests").child(uid).child(theirUID).removeValue();
                ref.child("friend_requests").child(theirUID).child(uid).removeValue();


            }
        });

        holder.mReject.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ref.child("friend_requests").child(uid).child(theirUID).removeValue();
                ref.child("friend_requests").child(theirUID).child(uid).removeValue();

            }
        });

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mName;
        ImageView mProfilePic;
        RelativeLayout mRelative;
        Button mAccept;
        Button mReject;

        ViewHolder(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.name);
            mProfilePic = itemView.findViewById(R.id.profilePic);
            mRelative = itemView.findViewById(R.id.card_relative);
            mAccept = itemView.findViewById(R.id.yes);
            mReject = itemView.findViewById(R.id.no);

        }
    }
    private void loadProfilePic(final String url, final ImageView mImageView){

        Picasso.get()
                .load(url)
                .resize(256, 256)
                .centerCrop()
                .into(mImageView);

    }

}
