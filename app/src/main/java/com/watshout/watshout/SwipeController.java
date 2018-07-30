package com.watshout.watshout;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private Context context;

    final ColorDrawable background = new ColorDrawable(Color.RED);

    RecyclerView.Adapter adapter;
    RecyclerView recyclerView;


    SwipeController(ArrayList<FriendItem> listItems, String myUID, Context context,
                    RecyclerView.Adapter adapter, RecyclerView recyclerView) {

        this.listItems = listItems;
        this.myUID = myUID;
        this.context = context;
        this.adapter = adapter;
        this.recyclerView = recyclerView;

        this.recyclerView.setItemAnimator(null);
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
        String theirName = current.getName();

        removeFriend(theirName, theirUID, index, adapter);

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


    private void removeFriend(String theirName, final String theirUID, final int index,
                              final RecyclerView.Adapter adapter) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        builder.setMessage("Are you sure you want to unfriend " + theirName + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        Log.d("FRIEND", "Removing friend");

                        ref.child("friend_data").child(myUID).child(theirUID).removeValue();
                        ref.child("friend_data").child(theirUID).child(myUID).removeValue();

                        adapter.notifyItemRemoved(index);
                        recyclerView.setAdapter(adapter);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        adapter.notifyItemChanged(index);
                        recyclerView.setAdapter(adapter);

                    }
                })
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {

                        adapter.notifyItemChanged(index);
                        recyclerView.setAdapter(adapter);

                    }
                })
                .create()
                .show();

    }

}