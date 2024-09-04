package com.investmango.hrconsole.api;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessaging;
import com.investmango.hrconsole.model.Authority;
import com.investmango.hrconsole.model.User;
import com.investmango.hrconsole.service.Constant;
import com.investmango.hrconsole.service.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class ApiClient extends Application {

    // Live
//     public static final String BASE_URL = "http://api.imconsole.in:8080/";

    // Local
    public static final String BASE_URL = "http://52.66.208.137:8282/";
//    public static final String BASE_URL = "https://api.gopropify.in/";

//            public static final String BASE_URL = "http://192.168.29.202:8080/";
    private final ApiInterface apiInterface;
    private final Context context;
    private Context appcontext;
    private SharedPreferences preferences;
    static String token = "";

    public ApiClient(Context context) {
        this.context = context;
        preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);

//        var preferences =context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);

//        String token = preferences.getString("token", "0");

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        httpClientBuilder.addInterceptor(loggingInterceptor);
        httpClientBuilder.addInterceptor(chain -> {
                    if (preferences != null && preferences.getString("token", "0") != null) {
                        token = preferences.getString("token", "0");
                    }
                    Request request = chain.request()
                            .newBuilder()
                            .addHeader("Authorization", token).build();
                    return chain.proceed(request);
                }).connectTimeout(1, TimeUnit.MINUTES)
                .readTimeout(1, TimeUnit.MINUTES)
                .writeTimeout(1, TimeUnit.MINUTES)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClientBuilder.build())
                .build();

        apiInterface = retrofit.create(ApiInterface.class);
    }

    public static Retrofit getClient() {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appcontext = this.context;
    }

    public ApiInterface getApiInterface() {
        return apiInterface;
    }

    public void logout() {
        // Clear the stored token in SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("token");
        editor.remove("Authority");
        editor.remove("userId");
        editor.apply();

        // Redirect the user to the login screen
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }


    public void loginUser(RequestBody requestBody, final LoginCallback callback) {
        Call<User> call = apiInterface.logInUser(requestBody);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    Log.e("response", "onResponse: " + response.body());
                    if (user != null) {
                        saveToken(user.getToken());
                        callback.onLoginSuccess();
                    } else {
                        callback.onLoginFailure("User object is null");
                    }
                } else {
                    handleErrorResponse(response, callback);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                handleFailure(t, callback);
                Log.e("response", "onFailure: " + t.getMessage());
            }
        });
    }

    private void saveToken(String token) {
        SharedPreferences preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        preferences.edit().clear().apply();
        preferences.edit().putString("token", token).apply();

    }

    private void handleErrorResponse(Response<User> response, LoginCallback callback) {
        String errorMessage = "Failed to connect internet";
        if (response.errorBody() != null) {
            try {
                JSONObject jsonObject = new JSONObject(response.errorBody().string());
                String message = jsonObject.optString("message");
                if (!message.isEmpty()) {
                    errorMessage = message;
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        showToast(errorMessage);
        callback.onLoginFailure(errorMessage);
    }

    private void handleFailure(Throwable t, LoginCallback callback) {
        showToast("Failed to connect internet");
        callback.onLoginFailure(t.getMessage());
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public void getCurrentUser(String token, final CurrentUserCallback callback) {
        Call<User> call = apiInterface.getCurrentUser(token);
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        long userId = user.getId();
                        // Store userId in SharedPreferences
                        SharedPreferences preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putLong("userId", userId);
                        editor.apply();

                        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                String fcmToken = task.getResult();
                                saveDeviceToken(fcmToken, userId);
                                Log.d("FCM Token", "FCM Token -->" + " " + fcmToken);
                            }
                        });

                        List<Authority> authorities = user.getAuthorities();
                        if (authorities != null && !authorities.isEmpty()) {
                            String authority = authorities.get(0).getAuthority();
                            Log.e("authority", "onResponse: "+authority );
                            if (authority.equals("ADMIN")) {
                                callback.onAdminLoggedIn(authority);

                                preferences.edit().putString("Authority", Constant.ADMIN).apply();

                                System.out.println("Authority: " + authority);
                            } else if (authority.equals("USER")) {
                                callback.onUserLoggedIn(authority);
                                preferences.edit().putString("Authority", Constant.USER).apply();

                                System.out.println("Authority: " + authority);
                            } else if (authority.equals("MANAGER")) {
                                callback.onManagerLoggedIn(authority);
                                preferences.edit().putString("Authority", Constant.MANAGER).apply();

                                System.out.println("Authority: " + authority);
                            } else {
                                callback.onLoginFailure("Unknown user authority");
                            }
                        } else {
                            callback.onLoginFailure("User authority is empty or null");
                        }
                    } else {
                        callback.onLoginFailure("Failed to get user details");
                    }
                } else {
                    callback.onLoginFailure("Failed to retrieve current user");
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                String errorMessage = "Error: " + t.getMessage();
                Log.e("LoginError", errorMessage);
                callback.onLoginFailure("Error: " + t.getMessage());

            }
        });
    }

    public interface LoginCallback {
        void onLoginSuccess();

        void onLoginFailure(String message);
    }

    public interface CurrentUserCallback {
        void onAdminLoggedIn(String userRole);

        void onUserLoggedIn(String userRole);

        void onManagerLoggedIn(String userRole);

        void onLoginFailure(String message);
    }

    private void saveDeviceToken(String fcmToken, Long id) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("deviceToken", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final RequestBody requestBody = RequestBody.create(jsonObject.toString(), MediaType.parse("application/json"));

        preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "0");

        Call<String> call = apiInterface.saveDeviceToken(token, requestBody, id);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                Log.i("PushNotification", "Device token sent successfully.");
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                Log.i("PushNotification", "Device token didn't send successfully.");
            }
        });
    }
}
