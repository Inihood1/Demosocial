package com.inihood.funspace.android.me.helper;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.inihood.funspace.android.me.MainActivity;
import com.inihood.funspace.android.me.R;

public class MessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        try {
            String messageTitle = remoteMessage.getNotification().getTitle();
            String messageBody = remoteMessage.getNotification().getBody();
            String click_action = remoteMessage.getNotification().getClickAction();

            NotificationCompat.Builder mBuilder = new NotificationCompat.
                    Builder(this, getString(R.string.default_notification_channel_id))
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(messageTitle)
                    .setContentText(messageBody)
                    .setAutoCancel(true);

            Intent intent  = new Intent(click_action);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0 ,intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(pendingIntent);

            int notificationId = (int) System.currentTimeMillis();
            NotificationManager notificationManager = (NotificationManager)
                    getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.notify(notificationId, mBuilder.build());
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
