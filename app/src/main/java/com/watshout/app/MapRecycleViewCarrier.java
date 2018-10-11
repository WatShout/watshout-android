package com.watshout.app;

import android.support.v7.widget.RecyclerView;

class MapRecycleViewCarrier {

    private RecyclerView recyclerView;

    MapRecycleViewCarrier(RecyclerView recyclerView){
        this.recyclerView = recyclerView;
    }

    public RecyclerView getRecyclerView() {
        return recyclerView;
    }
}

