package com.watshout.watshout;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class MapFriendAdapter extends RecyclerView.Adapter<MapFriendAdapter.ViewHolder> {

    private Context context;
    private List<MapFriendItem> listItems;

    MapFriendAdapter(List<MapFriendItem> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
        Log.d("FRIENDS", "Initializing MapFriendAdapter");
    }

    @NonNull
    @Override
    public MapFriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        Log.d("RECYCLE", "On create view holder");

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_friend_card, parent, false);

        return new MapFriendAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MapFriendAdapter.ViewHolder holder, int position) {

        MapFriendItem mapFriendItem = listItems.get(position);
        //mapFriendItem.getInitials();

        Log.d("RECYCLE", "I am logging from view holder");
        holder.mInitials.setText(mapFriendItem.getInitials());

    }

    @Override
    public int getItemCount() {

        Log.d("RECYCLE", "Running item count");
        Log.d("RECYCLE", listItems.size() + "");

        return listItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mInitials;

        LinearLayout mLinearLayout;

        ViewHolder(View itemView) {
            super(itemView);

            mInitials = itemView.findViewById(R.id.initials);
            mLinearLayout = itemView.findViewById(R.id.card_linear_layout);

        }
    }

}