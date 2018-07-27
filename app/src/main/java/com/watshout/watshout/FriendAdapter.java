package com.watshout.watshout;

import android.content.Context;
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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.ViewHolder> {

    private List<FriendItem> listItems;
    private Context context;

    FriendAdapter(List<FriendItem> listItems, Context context) {
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

        final FriendItem friendItem = listItems.get(position);
        holder.mName.setText(friendItem.getName());

        holder.mRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("FRIEND", "Clicked on " + friendItem.getUID());
            }
        });


        loadProfilePic(friendItem.getProfilePic(), holder.mProfilePic);

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mName;
        ImageView mProfilePic;
        RelativeLayout mRelative;

        ViewHolder(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.name);
            mProfilePic = itemView.findViewById(R.id.profilePic);
            mRelative = itemView.findViewById(R.id.card_relative);

        }
    }

    private void loadProfilePic(final String url, final ImageView mImageView){

        new Thread(new Runnable() {
            public void run(){
                try {
                    //download the drawable
                    final Drawable drawable = Drawable.createFromStream((InputStream) new URL(url).getContent(), "src");
                    //edit the view in the UI thread
                    mImageView.post(new Runnable() {
                        public void run() {
                            mImageView.setImageDrawable(drawable);
                        }
                    });
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

}
