package com.watshout.watshout;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.util.Log;
import android.view.View;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

import static android.support.v7.widget.helper.ItemTouchHelper.*;

class SwipeController extends Callback {

    DatabaseReference ref = FirebaseDatabase
            .getInstance()
            .getReference();

    ArrayList<FriendItem> listItems;
    String myUID;

    private Paint p = new Paint();

    final ColorDrawable background = new ColorDrawable(Color.RED);


    SwipeController(ArrayList<FriendItem> listItems, String myUID) {
        this.listItems = listItems;
        this.myUID = myUID;
    }

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, LEFT | RIGHT);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        int index = viewHolder.getAdapterPosition();

        FriendItem current = listItems.get(index);
        String theirUID = current.getUID();

        removeFriend(theirUID);

    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

        View itemView = viewHolder.itemView;
        float height = (float) itemView.getBottom() - (float) itemView.getTop();
        float width = height / 3;

        if(dX > 0){
            p.setColor(Color.parseColor("#D32F2F"));
            RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
            c.drawRect(background,p);
        } else {
            p.setColor(Color.parseColor("#D32F2F"));
            RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
            c.drawRect(background,p);
        }

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void removeFriend(String theirUID) {

        ref.child("friend_data").child(myUID).child(theirUID).removeValue();
        ref.child("friend_data").child(theirUID).child(myUID).removeValue();


    }




}