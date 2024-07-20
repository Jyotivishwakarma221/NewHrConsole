package com.investmango.hrconsole.user.notification;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.util.Log;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Start the alarm activity when the alarm triggers
        playAlarmSound(context);
        Intent alarmIntent = new Intent(context, AlarmActivity.class);
        alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(alarmIntent);
    }

    private void playAlarmSound(Context context) {
        try {
            Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            Ringtone ringtone = RingtoneManager.getRingtone(context, alarmSound);
            if (ringtone != null) {
                ringtone.play();
            }
        } catch (Exception e) {
            Log.e("AlarmReceiver", "Failed to play alarm sound: " + e.getMessage());
        }
    }
    }

