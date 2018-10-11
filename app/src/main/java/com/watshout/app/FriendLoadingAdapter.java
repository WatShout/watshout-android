package com.watshout.app;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class FriendLoadingAdapter extends RecyclerView.Adapter<FriendLoadingAdapter.ViewHolder> {

    private int howMany;

    FriendLoadingAdapter(int howMany) {
        this.howMany = howMany;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.friend_loading_card, parent, false);

        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {


    }

    @Override
    public int getItemCount() {
        return howMany;
    }


    class ViewHolder extends RecyclerView.ViewHolder {


        ViewHolder(View itemView) {
            super(itemView);


        }
    }



}
