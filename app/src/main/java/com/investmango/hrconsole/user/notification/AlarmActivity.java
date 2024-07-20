package com.investmango.hrconsole.user.notification;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.investmango.hrconsole.R;

public class AlarmActivity extends Activity {

    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm);

        // Retrieve the PendingIntent for the alarm
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0, alarmIntent, getPendingIntentFlag());

        // Set up the Stop Alarm button
        Button stopAlarmButton = findViewById(R.id.btn_stop_alarm);
        stopAlarmButton.setOnClickListener(v -> {
            stopAlarm();
            Toast.makeText(AlarmActivity.this, "Alarm stopped", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private int getPendingIntentFlag() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.FLAG_IMMUTABLE;
        } else {
            return PendingIntent.FLAG_UPDATE_CURRENT;
        }
    }

    private void stopAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            // Cancel the alarm
            alarmManager.cancel(pendingIntent);
        }
    }
}
