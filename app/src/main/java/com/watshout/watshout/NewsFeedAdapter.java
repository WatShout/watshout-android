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
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.watshout.watshout.pojo.Activity;

import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> {

    private List<Activity> listItems;
    private Context context;

    NewsFeedAdapter(List<Activity> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_feed_card_copy, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        Activity newsFeedItem = listItems.get(position);
        holder.mName.setText(newsFeedItem.getName());

        Long time = newsFeedItem.getTime();

        String formattedTime = epochToIso8601(time);

        holder.mTime.setText(formattedTime);
        holder.mActivityName.setText(newsFeedItem.getEventName());

        String initials = "";
        for (String s : newsFeedItem.getName().split(" ")) {
            initials += s.charAt(0);
        }
        holder.mInitials.setText(initials);
        holder.mActivityDistance.setText(newsFeedItem.getDistance());
        holder.mActivityTime.setText(newsFeedItem.getTimeElapsed());
        holder.mActivityPace.setText(newsFeedItem.getPace());

        loadMapImage(newsFeedItem.getMapLink(), holder.mMap);

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mName;
        TextView mTime;
        TextView mActivityName;
        TextView mActivityDistance;
        TextView mActivityTime;
        TextView mActivityPace;
        TextView mInitials;
        ImageView mMap;
        LinearLayout mLinearLayout;

        ViewHolder(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.news_feed_name);
            mTime = itemView.findViewById(R.id.news_feed_time);
            mMap = itemView.findViewById(R.id.news_feed_map);
            mActivityName = itemView.findViewById(R.id.news_feed_activity_name);
            mActivityDistance = itemView.findViewById(R.id.news_feed_activity_distance);
            mActivityTime = itemView.findViewById(R.id.news_feed_activity_time);
            mActivityPace = itemView.findViewById(R.id.news_feed_activity_pace);
            mInitials = itemView.findViewById(R.id.news_feed_initials);
            mLinearLayout = itemView.findViewById(R.id.card_linear_layout);

        }
    }

    private String epochToIso8601(long time) {
        Date date = new Date(time);
        DateFormat format = new SimpleDateFormat("MMM dd HH:mm");
        format.setTimeZone(TimeZone.getDefault());
        String formatted = format.format(date);

        return formatted;
    }

    private void loadMapImage(final String url, final ImageView mImageView){

        Picasso.get()
                .load(url)
                .into(mImageView);

    }

}
