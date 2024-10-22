package com.investmango.hrconsole.service;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.Stage;
import com.investmango.hrconsole.model.StagesResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommonUtils {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    static double percentage = 0;

    public static boolean isGpsEnabled(Activity context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean providerEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (providerEnabled) {
            return true;
        } else {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setMessage("Your Location seems to be disabled. Do you want to enable it?")
                    .setCancelable(false)
                    .setPositiveButton("Yes", (dialog, id) -> context.startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
            final AlertDialog alert = builder.create();
            alert.show();
        }
        return false;
    }

    public static boolean isAllPermissionsAllowed(Context context) {
        List<String> listPermissionsNeeded = new ArrayList<>();

        int locPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        if (locPerm != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) context, listPermissionsNeeded.toArray(new String[0]), LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }


    public static void openWhatsApp(Context context, String phoneNumber) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber;
            intent.setPackage("com.whatsapp");
            intent.setData(Uri.parse(url));
            if (intent.resolveActivity(packageManager) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e("ERROR WHATSAPP", e.toString());
            Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
        }
    }

    public static boolean isAllPermissionsAllowd(LoginActivity loginActivity) {
        List<String> listPermissionsNeeded = new ArrayList<>();

        int locPerm = ContextCompat.checkSelfPermission(loginActivity, Manifest.permission.ACCESS_FINE_LOCATION);
        if (locPerm != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions((Activity) loginActivity, listPermissionsNeeded.toArray(new String[0]), LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }

        return true;
    }

    public static void getProgress(int id, Context context, ProgressCallback callback) {
        ApiClient apiClient = new ApiClient(context);
        ApiInterface apiInterface = apiClient.getApiInterface();
        Call<StagesResponse> call = apiInterface.getStageById(id);

        call.enqueue(new Callback<StagesResponse>() {
            @Override
            public void onResponse(@NonNull Call<StagesResponse> call, @NonNull Response<StagesResponse> response) {
                if (response.isSuccessful()) {
                    List<Stage> content = response.body().getContent();
                    int size = content != null ? content.size() : 0;
                    int count = 0;

                    if (content != null) {
                        for (Stage stage : content) {
                            if ("COMPLETED".equals(stage.getStatus())) {
                                count++;
                            }
                        }
                    }

                    int percentage = 0;
                    if (size != 0) {
                        percentage = (int) ((count / (double) size) * 100);
                    }

                    Log.e("getProgress", "Calculated Percentage: " + percentage+ " "+ id);
                    callback.onProgressCalculated(percentage);
                } else {
                    Log.e("errorMsg", "onResponse: " + response.errorBody());
                    callback.onProgressCalculated(0);
                }
            }

            @Override
            public void onFailure(@NonNull Call<StagesResponse> call, @NonNull Throwable t) {
                Log.e("getProgress", "API Call Failed", t);
                Toast.makeText(context, "Failed to get progress.", Toast.LENGTH_SHORT).show();
                callback.onProgressCalculated(0);
            }
        });
    }
    public interface ProgressCallback {
        void onProgressCalculated(int progress);
    }
}
