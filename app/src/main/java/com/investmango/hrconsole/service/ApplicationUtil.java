package com.investmango.hrconsole.service;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ApplicationUtil {
    private static ScheduledExecutorService executor = null;
    private static boolean status = false;

    public static void runConnectionCheckThread(Activity activity) {
        if (executor == null) {
            executor = Executors.newSingleThreadScheduledExecutor();
            Runnable periodicTask = () -> {
                // Check network availability and update status
                status = isNetworkAvailable(activity);
            };
            executor.scheduleWithFixedDelay(periodicTask, 0, 10, TimeUnit.SECONDS);
        }
    }

    public static void stopConnectionCheckThread() {
        if (executor != null) {
            if (!executor.isShutdown()) {
                executor.shutdown();
            }
            executor = null;
        }
    }

    public static boolean isNetworkAvailable(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean getNetworkStatus() {
        return status;
    }
}
