package com.watshout.watshout;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.watshout.watshout.pojo.Friend;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private List<Friend> listItems;
    private Context context;

    FriendAdapter(List<Friend> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_card, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final Friend friendItem = listItems.get(position);
        holder.mName.setText(friendItem.getName());

        holder.mRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FRIEND ", "Clicked on " + friendItem.getUid());
            }
        });

        holder.mSince.setText(epochToIso8601(friendItem.getSince()));


        loadProfilePic(friendItem.getProfilePic(), holder.mProfilePic);

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }


    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mName;
        TextView mSince;
        ImageView mProfilePic;
        RelativeLayout mRelative;

        ViewHolder(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.name);
            mProfilePic = itemView.findViewById(R.id.profilePic);
            mRelative = itemView.findViewById(R.id.card_relative);
            mSince = itemView.findViewById(R.id.since);

        }
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
