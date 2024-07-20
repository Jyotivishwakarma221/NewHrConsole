package com.investmango.hrconsole.service;

import static com.investmango.hrconsole.service.ApplicationUtil.isNetworkAvailable;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.manager.activity.ManagerActivity;
import com.investmango.hrconsole.model.User;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST = 1001;
    private SharedPreferences preferences;
    private String token;
    private ApiInterface apiInterface;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    private ApiClient apiClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        apiClient = new ApiClient(this);
        apiInterface = apiClient.getApiInterface();

        checkAppPermissions();

        if (!CommonUtils.isAllPermissionsAllowed(SplashActivity.this)) {
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(SplashActivity.this);
            alertDialogBuilder.setTitle("GPS Required");
            alertDialogBuilder.setMessage("Please allow GPS to proceed.");
            alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            });
            alertDialogBuilder.setNegativeButton("Cancel", null);
            alertDialogBuilder.show();
            return;
        }
        if (CommonUtils.isGpsEnabled(this)){
            // Use a named constant for the delay time to improve code readability
            int splashScreenDuration = 3500;

            Handler handler = new Handler();
            handler.postDelayed(this::NavigateToActivty, splashScreenDuration);
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("TAGRestart", "onResume: " );
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("TAGRestart", "onStart: "  );
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("TAGRestart", "onRestart: " );

        if (CommonUtils.isGpsEnabled(this)){
            // Use a named constant for the delay time to improve code readability
            int splashScreenDuration = 2000;

            Handler handler = new Handler();
            handler.postDelayed(this::NavigateToActivty, splashScreenDuration);
        }
    }
    //    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           @NonNull String[] permissions,
//                                           @NonNull int[] grantResults)
//    {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//
//        if (requestCode == CAMERA_PERMISSION_CODE) {
//
//            // Checking whether user granted the permission or not.
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                // Showing the toast message
//                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show();
//            }
//            else {
//                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show();
//            }
//
//        }
//    }


    private void checkAppPermissions() {

        if ((ContextCompat.checkSelfPermission(
                this,
                Manifest.permission_group.READ_MEDIA_VISUAL
        ) != PackageManager.PERMISSION_GRANTED) ||
                (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.INTERNET
                ) != PackageManager.PERMISSION_GRANTED) ||
                ((ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED)) ||
                ((ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_NETWORK_STATE
                ) != PackageManager.PERMISSION_GRANTED)) ||
                ((ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)) ||
                ((ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED)) ||
                ((ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED))
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.INTERNET
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.CAMERA
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_NETWORK_STATE
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
            )
            ) {

//                GET_API_GET_APP_VERSION()
            } else {
                ActivityCompat.requestPermissions(
                        this, new String[]{
                                android.Manifest.permission_group.READ_MEDIA_VISUAL,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.INTERNET,
                                android.Manifest.permission.CAMERA,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_NETWORK_STATE,
                        }, MY_PERMISSIONS_REQUEST);


            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (!allPermissionsGranted) {
                // Handle the case when not all permissions are granted
            }
        }
    }
    private void navigateToLoginActivity() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void NavigateToActivty() {
        preferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        Log.e("NavigateToActivty", "NavigateToActivty: " + token);
        // Refresh Token
        if (token != null && !token.equals("0")) {
            refreshToken();
            apiClient.getCurrentUser(token, new ApiClient.CurrentUserCallback() {
                @Override
                public void onAdminLoggedIn(String userRole) {
                    preferences.edit().putString("Authority", Constant.ADMIN).apply();
                    redirectToManagerActivity();
                }

                @Override
                public void onUserLoggedIn(String userRole) {

                    preferences.edit().putString("Authority", Constant.USER).apply();
                    redirectToManagerActivity();
                }

                @Override
                public void onManagerLoggedIn(String userRole) {

                    preferences.edit().putString("Authority", Constant.MANAGER).apply();
                    redirectToManagerActivity();
                }

                @Override
                public void onLoginFailure(String message) {
                    if (message.equals("User authority is empty.")) {
                        Toast.makeText(SplashActivity.this, "Login failed: User authority is empty.", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else navigateToLoginActivity();

    }

    public void refreshToken() {
        if (isNetworkAvailable(this)) {
            preferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
            String refreshToken = preferences.getString("token", "0");

            Log.e("Refresh Token", "Attempting to refresh token with refresh token: " + refreshToken);

            // Create the JSON request body
            JSONObject jsonBody = new JSONObject();
            try {
                jsonBody.put("refreshToken", refreshToken);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Create the request body (JSON)
            RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
            Call<User> call = apiInterface.refreshToken(requestBody);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    if (response.isSuccessful()) {
                        User user = response.body();
                        assert user != null;
                        String newAccessToken = user.getToken();
                        Log.e("Refresh Token", "New Access Token: " + newAccessToken);

                        // Update the stored token in SharedPreferences
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("token", newAccessToken);
                        editor.apply();

                    } else {
                        try {
                            JSONObject errorBody = new JSONObject(response.errorBody().string());
                            String errorMessage = errorBody.getString("message");
                            Log.e("Refresh Token", "Token refresh failed. Error message: " + errorMessage);
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                            navigateToLoginActivity();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e("Refresh Token", "Token refresh failed. Unknown error occurred.");
                        }
                        ;
                    }
                }

                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    Log.e("Refresh Token", "Token refresh failed. Error message: " + t.getMessage());
                    Toast.makeText(getApplicationContext(), "Failed to Connect Internet.", Toast.LENGTH_LONG).show();
                }
            });
        } else Toast.makeText(this, "No Internet Connection", Toast.LENGTH_SHORT).show();

    }

    private void redirectToManagerActivity() {
        Intent intent = new Intent(this, ManagerActivity.class);
        System.out.println("Manager Intent: " + intent);
        startActivity(intent);
        finish();
    }


}
