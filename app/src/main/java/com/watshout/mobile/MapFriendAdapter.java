package com.watshout.mobile;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class MapFriendAdapter extends RecyclerView.Adapter<MapFriendAdapter.ViewHolder> {

    private Context context;
    private List<FriendCurrentLocation> listItems;
    private GoogleMap googleMap;


    MapFriendAdapter(HashMap<String, FriendCurrentLocation> listItems, Context context, GoogleMap googleMap) {

        Collection<FriendCurrentLocation> values = listItems.values();

        this.listItems = new ArrayList<>(values);
        this.context = context;
        this.googleMap = googleMap;

    }

    @NonNull
    @Override
    public MapFriendAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.map_friend_card, parent, false);

        return new MapFriendAdapter.ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final MapFriendAdapter.ViewHolder holder, int position) {

        final FriendCurrentLocation friendCurrentLocation = listItems.get(position);
        holder.mInitials.setText(friendCurrentLocation.getInitials());

        holder.mCardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recenterMap(friendCurrentLocation.getCoords());
                googleMap.setMapStyle(
                        MapStyleOptions.loadRawResourceStyle(context, R.raw.google_map_color
                        ));
            }
        });

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        TextView mInitials;
        CardView mCardView;
        LinearLayout mLinearLayout;

        ViewHolder(View itemView) {
            super(itemView);

            mInitials = itemView.findViewById(R.id.initials);
            mCardView = itemView.findViewById(R.id.initialsBubble);
            mLinearLayout = itemView.findViewById(R.id.card_linear_layout);

        }
    }

    private void recenterMap(LatLng coords) {

        googleMap.moveCamera(CameraUpdateFactory
                .newLatLngZoom(coords, 16));

    }

}