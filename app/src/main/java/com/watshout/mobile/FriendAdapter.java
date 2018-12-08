package com.watshout.mobile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;
import com.watshout.mobile.pojo.Friend;
import com.watshout.mobile.pojo.FriendObject;
import com.watshout.mobile.pojo.FriendRequest;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public List<FriendObject> listItems;
    private Context context;
    public int cutoff;

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String email = thisUser.getEmail();
    String uid = thisUser.getUid();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    FriendAdapter(List<FriendObject> listItems, Context context, int cutoff) {
        this.listItems = listItems;
        this.context = context;
        this.cutoff = cutoff;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view;

        switch (viewType) {
            case 0:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.friend_card, parent, false);
                return new ViewHolderFriend(view);
            case 1:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.friend_request_card, parent, false);
                return new ViewHolderRequest(view);
            default:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.friend_card, parent, false);
                return new ViewHolderFriend(view);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position < cutoff) {
            return 0;
        } else {
            return 1;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {

        if (holder.getItemViewType() == 0) {
            ViewHolderFriend vhf = (ViewHolderFriend) holder;
            Friend friendItem = (Friend) listItems.get(position);
            vhf.mName.setText(friendItem.getName());

            vhf.mRelative.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("FRIEND ", "Clicked on " + friendItem.getUid());
                }
            });

            vhf.mSince.setText(epochToIso8601(friendItem.getSince()));

            loadProfilePic(friendItem.getProfilePic(), vhf.mProfilePic);
        } else {
            ViewHolderRequest vhr = (ViewHolderRequest) holder;
            FriendRequest friendRequestItem = (FriendRequest) listItems.get(position);

            vhr.mName.setText(friendRequestItem.getName());
            loadProfilePic(friendRequestItem.getProfilePic(), vhr.mProfilePic);

            final String theirUID = friendRequestItem.getUid();

            final String millis = Long.toString(System.currentTimeMillis());

            vhr.mAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    removeItem(position);

                    ref.child("friend_data").child(uid).child(theirUID).setValue(millis);
                    ref.child("friend_data").child(theirUID).child(uid).setValue(millis);

                    ref.child("friend_requests").child(uid).child(theirUID).removeValue();
                    ref.child("friend_requests").child(theirUID).child(uid).removeValue();

                    Friend newFriend = new Friend();
                    newFriend.setName(friendRequestItem.getName());
                    newFriend.setUid(theirUID);
                    newFriend.setProfilePic(friendRequestItem.getProfilePic());
                    newFriend.setSince(Long.valueOf(millis));

                    listItems.add(newFriend);
                    cutoff += 1;
                    notifyItemInserted(listItems.size() - 1);
                    notifyDataSetChanged();

                }
            });

            vhr.mReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    removeItem(position);

                    ref.child("friend_requests").child(uid).child(theirUID).removeValue();
                    ref.child("friend_requests").child(theirUID).child(uid).removeValue();

                }
            });

        }

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }


    class ViewHolderFriend extends RecyclerView.ViewHolder {

        TextView mName;
        TextView mSince;
        ImageView mProfilePic;
        RelativeLayout mRelative;

        ViewHolderFriend(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.name);
            mProfilePic = itemView.findViewById(R.id.profilePic);
            mRelative = itemView.findViewById(R.id.card_relative);
            mSince = itemView.findViewById(R.id.since);

        }
    }

    class ViewHolderRequest extends RecyclerView.ViewHolder {

        TextView mName;
        ImageView mProfilePic;
        RelativeLayout mRelative;
        Button mAccept;
        Button mReject;

        ViewHolderRequest(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.name);
            mProfilePic = itemView.findViewById(R.id.profilePic);
            mRelative = itemView.findViewById(R.id.card_relative);
            mAccept = itemView.findViewById(R.id.yes);
            mReject = itemView.findViewById(R.id.no);

        }
    }

    private void removeItem(int position){
        listItems.remove(position);
        notifyItemRemoved(position);
    }

    private String epochToIso8601(long time) {
        Date date = new Date(time);
        DateFormat format = new SimpleDateFormat("MMM dd");
        format.setTimeZone(TimeZone.getDefault());
        String formatted = format.format(date);

        return formatted;
    }

    private void loadProfilePic(final String url, final ImageView mImageView){

        Picasso.get()
                .load(url)
                .resize(256, 256)
                .transform(new CircleTransform())
                .placeholder(R.drawable.loading)
                .into(mImageView);

    }


}
