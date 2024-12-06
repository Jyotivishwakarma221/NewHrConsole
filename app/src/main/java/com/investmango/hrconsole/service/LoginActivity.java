package com.investmango.hrconsole.service;

import static com.investmango.hrconsole.service.ApplicationUtil.isNetworkAvailable;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.abhaysapp.awesomeprogressdialog.AwesomeProgressDialog;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.manager.activity.ManagerActivity;
import com.investmango.hrconsole.newHomePage.ForgetPasswordActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.sentry.Sentry;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText, passwordEditText;
    private ImageView passwordToggle;
    private ApiInterface apiInterface;
    private String token;
    private long userId;
    private SharedPreferences preferences;
    private boolean passwordVisible = false;
    private ApiClient apiClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    //    private TextView signUp;
    AwesomeProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        progressDialog = new AwesomeProgressDialog(this);
        progressDialog.addTitle("Loading..."); // add your title here.
        progressDialog.setStyle(AwesomeProgressDialog.STYLE_LOADING_DOTS);
        progressDialog.isCancelable(false);


        Sentry.init(options -> {
            options.setDsn("https://215c72dcf1cc7e9534590f4e675c0556@o4508319798001664.ingest.us.sentry.io/4508319808552960");

            // Set traces_sample_rate to 1.0 to capture 100%
            // of transactions for tracing.
            // We recommend adjusting this value in production.
            options.setTracesSampleRate(1.0);
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            int backStackEntryCount = getSupportFragmentManager().getBackStackEntryCount();
            if (backStackEntryCount == 0) {
                CardView goneLayout = findViewById(R.id.cardView);
                if (goneLayout != null) {
                    goneLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        apiClient = new ApiClient(LoginActivity.this);
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        passwordToggle = findViewById(R.id.passwordToggle);
        TextView loginButton = findViewById(R.id.loginButton);
        ApiClient apiClient = new ApiClient(this);
        apiInterface = apiClient.getApiInterface();
        preferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
//        signUp = findViewById(R.id.signUp);
//
//        signUp.setOnClickListener(view -> {
//            CardView goneLayout = findViewById(R.id.cardView);
//            if (goneLayout != null) {
//                goneLayout.setVisibility(View.GONE);
//            }

//            // Open SignUpFragment
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.replace_container, new SignUpFragment())
//                    .addToBackStack(null)
//                    .commit();
//        });

        TextView forgetButton = findViewById(R.id.forgetButton);

        forgetButton.setOnClickListener(v -> {
            startActivity(new Intent(this, ForgetPasswordActivity.class));
//            showForgetPasswordDialog();
        });


        loginButton.setOnClickListener(v -> {
            if (isNetworkAvailable(LoginActivity.this)) {

                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString();

                if (TextUtils.isEmpty(username)) {
                    usernameEditText.setError("Please enter a username");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    passwordEditText.setError("Please enter a password");
                    return;
                }

//                if (!CommonUtils.isAllPermissionsAllowed(LoginActivity.this)) {
//                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(LoginActivity.this);
//                    alertDialogBuilder.setTitle("GPS Required");
//                    alertDialogBuilder.setMessage("Please allow GPS to proceed.");
//                    alertDialogBuilder.setPositiveButton("OK", (dialog, which) -> {
//                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                        startActivity(intent);
//                    });
//                    alertDialogBuilder.setNegativeButton("Cancel", null);
//                    alertDialogBuilder.show();
//                    return;
//                }

                // Create the JSON request body
                JSONObject jsonBody = new JSONObject();
                try {
                    jsonBody.put("userName", username);
                    jsonBody.put("password", password);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                progressDialog.showDialog();
                RequestBody requestBody = RequestBody.create(jsonBody.toString(), MediaType.parse("application/json; charset=utf-8"));
                apiClient.loginUser(requestBody, new ApiClient.LoginCallback() {
                    @Override
                    public void onLoginSuccess() {
                        Toast.makeText(LoginActivity.this, "Login Successfully", Toast.LENGTH_SHORT).show();
                        preferences = getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                        token = preferences.getString("token", "0");
                        try {
                            progressDialog.dismissDialog();

                        } catch (Exception e) {
                            Log.e("Exception", "onLoginSuccess: " + e);
                        }

                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        // Submit a task to the ExecutorService
                        executor.execute(() -> {
                            try {
                                AccessToken accessToken = new AccessToken();
                                String data = accessToken.getAccessToken();
                                // Logging the access token
                                Log.e("AccessToken", "AccessToken is: " + data);
                            } catch (Exception e) {
                                Log.e("AccessTokenExecp", "Error fetching access token", e);
                            }
                        });

                        // Shutdown the executor when done, if necessary
                        executor.shutdown();

                        apiClient.getCurrentUser(token, new ApiClient.CurrentUserCallback() {
                            @Override
                            public void onAdminLoggedIn(String userRole) {
                                SharedPreferences preferences = getApplicationContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                                preferences.edit().putString("Authority", Constant.ADMIN).apply();
                                Intent intent = new Intent(LoginActivity.this, ManagerActivity.class);
                                System.out.println("Admin Intent : " + intent);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onUserLoggedIn(String userRole) {
                                SharedPreferences preferences = getApplicationContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                                preferences.edit().putString("Authority", Constant.USER).apply();

                                Intent intent = new Intent(LoginActivity.this, ManagerActivity.class);
                                System.out.println("User Intent : " + intent);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onManagerLoggedIn(String userRole) {
                                SharedPreferences preferences = getApplicationContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
                                preferences.edit().putString("Authority", Constant.MANAGER).apply();

                                Intent intent = new Intent(LoginActivity.this, ManagerActivity.class);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void onLoginFailure(String message) {
                                progressDialog.dismissDialog();

                                if (message.equals("User authority is empty.")) {
                                    Toast.makeText(LoginActivity.this, "Login failed: User authority is empty.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }

                    @Override
                    public void onLoginFailure(String message) {
                        progressDialog.dismissDialog();

                        if (message.equals("User authority is empty.")) {
                            Toast.makeText(LoginActivity.this, "Login failed: User authority is empty.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                progressDialog.dismissDialog();

                Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }
        });
        passwordToggle.setOnClickListener(v -> togglePasswordVisibility());
    }


    // Forget Password
    private void showForgetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_forget_password, null);
        EditText emailEditText = dialogView.findViewById(R.id.editTextEmail);

        builder.setTitle("Forget Password")
                .setMessage("Enter your email to receive an OTP.")
                .setView(dialogView)
                .setPositiveButton("Send OTP", (dialog, which) -> {
                    // Handle the "Send OTP" button click
                    String email = emailEditText.getText().toString().trim();
                    if (TextUtils.isEmpty(email)) {
                        emailEditText.setError("Please enter your email");
                    } else {
                        // Call the sendOtp API
                        if (isNetworkAvailable(LoginActivity.this)) {
                            Call<ResponseBody> call = apiInterface.sendOtp(email);
                            call.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        if (response.body() != null) {
                                            try {
                                                // Parse the response body if it's not null
                                                String responseBodyString = response.body().string();
                                                JSONObject responseObject = new JSONObject(responseBodyString);
                                                String apiMessage = responseObject.getString("message");
                                                Toast.makeText(LoginActivity.this, apiMessage, Toast.LENGTH_SHORT).show();
                                                showOtpInputDialog(email);
                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            // Handle the case where the response body is null
                                            Toast.makeText(LoginActivity.this, "Response body is empty", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                    if (t instanceof IOException) {
                                        Toast.makeText(LoginActivity.this, "Network error. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Non-network error
                                        String errorMessage = "Failed to send OTP. Please try again later.";

                                        Log.e("API_ERROR", "Error: " + t.getMessage());

                                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
    }

    private void showOtpInputDialog(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_otp_input, null);
        EditText otpEditText = dialogView.findViewById(R.id.editTextOtp);

        builder.setTitle("Enter OTP")
                .setMessage("Enter the OTP sent to your email.")
                .setView(dialogView)
                .setPositiveButton("Verify", (dialog, which) -> {
                    // Handle the "Verify" button click
                    String otp = otpEditText.getText().toString().trim();
                    if (TextUtils.isEmpty(otp)) {
                        otpEditText.setError("Please enter the OTP");
                    } else {
                        // Call the verifyOtp API
                        if (isNetworkAvailable(LoginActivity.this)) {
                            Call<ResponseBody> verifyCall = apiInterface.verifyOtp(email, otp);
                            verifyCall.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        if (response.body() != null) {
                                            try {
                                                // Parse the response body if it's not null
                                                String responseBodyString = response.body().string();
                                                JSONObject responseObject = new JSONObject(responseBodyString);
                                                String apiMessage = responseObject.getString("message");
                                                Toast.makeText(LoginActivity.this, apiMessage, Toast.LENGTH_SHORT).show();
                                                changePassword(email);
                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    } else {
                                        ResponseBody errorBody = response.errorBody();
                                        if (errorBody != null) {
                                            try {
                                                String errorResponse = errorBody.string();
                                                JSONObject errorObject = new JSONObject(errorResponse);
                                                String errorMessage = errorObject.getString("message");
                                                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {

                                }

                            });
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
    }

    private void changePassword(String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ApiClient apiClient = new ApiClient(this);

        // Inflate the custom layout
        View dialogView = getLayoutInflater().inflate(R.layout.sendpassword, null);
        EditText newPasswordEditText = dialogView.findViewById(R.id.newPassword);
        EditText confirmPasswordEditText = dialogView.findViewById(R.id.confirmPassword);

        builder.setTitle("Enter New Password and ConfirmPassword")
                .setView(dialogView)
                .setPositiveButton("Reset Password", (dialog, which) -> {
                    // Handle the "Verify" button click
                    String newPassword = newPasswordEditText.getText().toString().trim();
                    String confirmPassword = confirmPasswordEditText.getText().toString().trim();

                    if (TextUtils.isEmpty(newPassword)) {
                        newPasswordEditText.setError("Please enter the new password");
                    } else if (TextUtils.isEmpty(confirmPassword)) {
                        confirmPasswordEditText.setError("Please enter the confirm password");
                    } else if (!newPassword.equals(confirmPassword)) {
                        // Check if new password matches confirm password
                        confirmPasswordEditText.setError("Passwords do not match");
                        Toast.makeText(LoginActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    } else {
                        // Call the resetPassword API
                        if (isNetworkAvailable(LoginActivity.this)) {
                            Call<ResponseBody> verifyCall = apiInterface.resetPassword(email, newPassword);
                            verifyCall.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                                    if (response.isSuccessful()) {
                                        if (response.body() != null) {
                                            try {
                                                // Parse the response body if it's not null
                                                String responseBodyString = response.body().string();
                                                JSONObject responseObject = new JSONObject(responseBodyString);
                                                String apiMessage = responseObject.getString("message");
                                                Toast.makeText(LoginActivity.this, apiMessage, Toast.LENGTH_SHORT).show();
                                            } catch (IOException | JSONException e) {
                                                e.printStackTrace();
                                            }
                                        } else {
                                            // Handle the case where the response body is null
                                            Toast.makeText(LoginActivity.this, "Response body is empty", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }

                                @Override
                                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                                    if (t instanceof IOException) {
                                        // Network error
                                        Toast.makeText(LoginActivity.this, "Network error. Please check your internet connection.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        // Non-network error
                                        String errorMessage = "Failed to verify OTP";

                                        Log.e("API_ERROR", "Error: " + t.getMessage());

                                        Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            // Display a Toast message if there is no internet connection
                            Toast.makeText(LoginActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                })
                .show();
    }


    private String formatDate(long eventDateTime) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        return sdf.format(new Date(eventDateTime));
    }

    private String formatTime(long eventDateTime) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", Locale.getDefault());
        return sdf.format(new Date(eventDateTime));
    }

    private void togglePasswordVisibility() {
        if (passwordVisible) {
            passwordEditText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.ic_open_eye);
        } else {
            passwordEditText.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            passwordToggle.setImageResource(R.drawable.ic_close_eye);
        }
        passwordVisible = !passwordVisible;

        // Move the cursor to the end of the password EditText
        passwordEditText.setSelection(passwordEditText.getText().length());
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
//            boolean allPermissionsGranted = true;
//            for (int result : grantResults) {
//                if (result != PackageManager.PERMISSION_GRANTED) {
//                    allPermissionsGranted = false;
//                    break;
//                }
//            }
//
//            if (!allPermissionsGranted) {
//                // Handle the case when not all permissions are granted
//            }
//        }
//    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        CardView goneLayout = findViewById(R.id.cardView);
        if (goneLayout != null) {
            goneLayout.setVisibility(View.VISIBLE);
        }
    }

}
