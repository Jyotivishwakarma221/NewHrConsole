package com.investmango.hrconsole.user.notification;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.manager.activity.ManagerActivity;

public class FirebaseMsgService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.d("Refreshed Token", s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        Log.d("FCM Data Received", "getData ->" + remoteMessage.getData());
        super.onMessageReceived(remoteMessage);

        String title = remoteMessage.getData().get("title");
        String body = remoteMessage.getData().get("body");

        // Check if data payload (title and body) is available
        if (title != null && body != null) {
            pushNotification(title, body);
        } else if (remoteMessage.getNotification() != null) {
            pushNotification(
                    remoteMessage.getNotification().getTitle(),
                    remoteMessage.getNotification().getBody()
            );
        }
    }

    private void pushNotification(String title, String msg) {
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Notification notification;
        final String CHANNEL_ID = "push_notification";

        Intent intent;
        // Check the title to decide which fragment to open
        switch (title) {
            case "Today Task":
            case "Task":
                intent = new Intent(this, ManagerActivity.class);
                intent.putExtra("fragmentToLoad", "TaskFragment");
                break;
            case "Leaves":
                intent = new Intent(this, ManagerActivity.class);
                intent.putExtra("fragmentToLoad", "UserLeaveFragment");
                break;
            case "Attendance Reminder":
                intent = new Intent(this, ManagerActivity.class);
                intent.putExtra("fragmentToLoad", "UserAttendanceFragment");
                break;
//            case "Event":
//                intent = new Intent(this, UserHomeActivity.class);
//                break;
            case "Message":
                intent = new Intent(this, ManagerActivity.class);
                intent.putExtra("fragmentToLoad", "MessageFragment");
                break;
            case "New Meeting":
                intent = new Intent(this, ManagerActivity.class);
                intent.putExtra("fragmentToLoad", "MeetingFragment");
                break;
            default:
                intent = new Intent(this, ManagerActivity.class);
                break;
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pi;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            // Use FLAG_IMMUTABLE for Android 12 (API level 31) and above
            pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        } else {
            // For older Android versions, use FLAG_UPDATE_CURRENT
            pi = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Custom Channel";
            String description = "Channel for Push Notification";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            if (nm != null) {
                nm.createNotificationChannel(channel);
            }

            notification = new Notification.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.iconhr)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .build();
        } else {
            notification = new Notification.Builder(this)
                    .setSmallIcon(R.drawable.iconhr)
                    .setContentTitle(title)
                    .setContentText(msg)
                    .setAutoCancel(true)
                    .setContentIntent(pi)
                    .build();
        }

        // Notify using the notification manager
        if (nm != null) {
            nm.notify(0, notification);
        }
    }

}
