package com.investmango.hrconsole.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.model.LocationModel;
import com.investmango.hrconsole.user.adapter.FirebaseService.FirebaseConnection;

import java.util.Objects;

public class MyBackgroundLocationService extends Service {
    private static final int NOTIFICATION_ID = 1;
    public static final String TAG = MyBackgroundLocationService.class.getSimpleName();
    private FusedLocationProviderClient mLocationClient;
    private LocationCallback mLocationCallback;
    private SharedUtils sharedUtils;

    public MyBackgroundLocationService() {

    }
    @Override
    public void onCreate() {
        super.onCreate();
        mLocationClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());
        sharedUtils = new SharedUtils(getApplicationContext());
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                Location location = locationResult.getLastLocation();
                userLocationResult(Objects.requireNonNull(location));
            }
        };
    }

    private void userLocationResult(Location locations) {
        LocationModel locationModel = new LocationModel();
        double latitude = locations.getLatitude();
        double longitude = locations.getLongitude();
        locationModel.setDateAndTime(DateAndTimeUtility.getCurrentDateAndTime());
        locationModel.setLatitude(latitude);
        locationModel.setLongitude(longitude);

        // Retrieve user ID from SharedPreferences
        SharedPreferences preferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        long userId = preferences.getLong("userId", 0);
        // Update the location model with the user ID
        locationModel.setUserId(userId);
        //        Log.d("Background","userId" + userId);
        FirebaseConnection.saveCoordinates(locationModel, userId);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand : Called.");
        // Create a notification channel for Android Oreo and higher.
        createNotificationChannel();
        try {
            startForeground(NOTIFICATION_ID, getNotification());
            getLocationUpdate();
        }catch (Exception e){
            Log.e(TAG, "onStartCommand: "+e.getMessage() );
        }
        return START_STICKY;
    }
    // Create a notification channel for Android Oreo and higher.
    private void createNotificationChannel() {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("Notification", "MyChannel", NotificationManager.IMPORTANCE_DEFAULT);
        }
        NotificationManager notificationManager = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            notificationManager = getSystemService(NotificationManager.class);
        }
        if (notificationManager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    private Notification getNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "Notification")
                .setContentText(" Service is running in the background.")
                .setSmallIcon(R.mipmap.ic_launcher);
        return builder.build();
    }
    private void getLocationUpdate() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(1000);
        locationRequest.setMaxWaitTime(10 * 1000);
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }
        mLocationClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy : Called.");
        stopForeground(true);
        mLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
