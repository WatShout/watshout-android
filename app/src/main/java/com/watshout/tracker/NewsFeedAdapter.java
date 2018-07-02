package com.watshout.tracker;

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

    private List<NewsFeedItem> listItems;
    private Context context;

    NewsFeedAdapter(List<NewsFeedItem> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_feed_card, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        NewsFeedItem newsFeedItem = listItems.get(position);
        holder.mName.setText(newsFeedItem.getName());

        Long time = Long.parseLong(newsFeedItem.getTime());

        String formattedTime = epochToIso8601(time);

        holder.mTime.setText(formattedTime);

        loadMapImage(newsFeedItem.getImageURL(), holder.mMap);

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mName;
        TextView mTime;
        ImageView mMap;
        LinearLayout mLinearLayout;

        ViewHolder(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.name);
            mTime = itemView.findViewById(R.id.time);
            mMap = itemView.findViewById(R.id.map);
            mLinearLayout = itemView.findViewById(R.id.card_linear_layout);

        }
    }

    private String epochToIso8601(long time) {
        Date date = new Date(time);
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        format.setTimeZone(TimeZone.getDefault());
        String formatted = format.format(date);

        return formatted;
    }

    private void loadMapImage(final String url, final ImageView mImageView){

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
