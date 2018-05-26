package com.watshout.face;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

public class MyNotificationManager {

    private Context context;
    private static MyNotificationManager mInstance;

    private MyNotificationManager(Context context){
        this.context = context;
    }

    public static synchronized MyNotificationManager getInstance(Context context) {

        if (mInstance == null){
            mInstance = new MyNotificationManager(context);
        }

        return mInstance;

    }

    public void displayNotification(String title, String body) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setSmallIcon(R.drawable.current)
                .setContentTitle(title)
                .setContentText(body);

        Intent intent = new Intent(context, MainActivity.class);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (mNotificationManager != null){

            mNotificationManager.notify(1, mBuilder.build());

        }

    }

}
