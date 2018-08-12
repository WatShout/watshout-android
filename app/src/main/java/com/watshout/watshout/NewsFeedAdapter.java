package com.watshout.watshout;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Point;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import com.watshout.watshout.pojo.Activity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class NewsFeedAdapter extends RecyclerView.Adapter<NewsFeedAdapter.ViewHolder> {

    private List<Activity> listItems;
    private Context context;
    private boolean isHistory;

    FirebaseUser thisUser = FirebaseAuth.getInstance().getCurrentUser();
    String uid = thisUser.getUid();

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    NewsFeedAdapter(List<Activity> listItems, Context context, boolean isHistory) {
        this.listItems = listItems;
        this.context = context;
        this.isHistory = isHistory;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.news_feed_card, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        final Activity newsFeedItem = listItems.get(position);

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

        int timeElapsed = Integer.valueOf(newsFeedItem.getTimeElapsed());
        String formattedElapsedTime = TimeManipulator.getInstance().formatTime(timeElapsed);
        holder.mActivityTime.setText(formattedElapsedTime);

        holder.mActivityPace.setText(newsFeedItem.getPace());

        loadMapImage(newsFeedItem.getMapLink(), holder.mMap);

        if (isHistory) {

            holder.mDeleteActivity.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("Are you sure you want to delete this activity?");

                    // Set up the buttons
                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            Log.d("NEWS", newsFeedItem.getActivityID());

                            ref.child("users").child(uid).child("device").child("past")
                                    .child(newsFeedItem.getActivityID()).removeValue();

                            listItems.remove(position);
                            notifyDataSetChanged();

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    builder.show();

                }
            });
        } else {
            holder.mDeleteActivity.setVisibility(View.INVISIBLE);
        }

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
        ImageView mDeleteActivity;

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
            mDeleteActivity = itemView.findViewById(R.id.deleteActivity);

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

        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int witdh = size.x;
        int height = size.y;

        Picasso.get()
                .load(url)
                .resize(witdh, height)
                .centerInside()
                .into(mImageView);

    }

}
