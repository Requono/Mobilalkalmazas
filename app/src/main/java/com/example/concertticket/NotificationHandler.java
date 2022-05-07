package com.example.concertticket;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHandler {
    private Context context;
    private NotificationManager manager;
    private static final String CHANNEL_ID = "notification_channel";

    public NotificationHandler(Context context) {
        this.context = context;
        this.manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        createChannel();
    }

    private void createChannel() {
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return;

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                "Application Notification",
                NotificationManager.IMPORTANCE_DEFAULT);

        channel.enableLights(true);
        channel.enableVibration(true);
        channel.setLightColor(Color.RED);
        channel.setDescription("A sudden notification appeared!");
        this.manager.createNotificationChannel(channel);
    }

    public void send(String message) {
        Intent intent = new Intent(context, ConcertTicketListActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);


        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle("Concert Ticket")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_cart)
                .setContentIntent(pendingIntent);

        this.manager.notify(0,builder.build());
    }
}
