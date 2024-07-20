package com.investmango.hrconsole.service;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Popup extends Activity {
    private static ApiInterface apiInterface;
    private static SharedPreferences preferences;
    private static String token;
    private static long userId;

    public static void changePassword(Context context) {
        ApiClient apiClient = new ApiClient(context);
        apiInterface = apiClient.getApiInterface();

        preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        // Create a dialog
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.fragment_update_password);

        // Set dialog size and gravity
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        dialog.getWindow().setAttributes(lp);

        // Find views by their IDs in the dialog's layout
        EditText newPassword = dialog.findViewById(R.id.new_Password);
        EditText confirmPassword = dialog.findViewById(R.id.confirm_Password);
        Button updateButton = dialog.findViewById(R.id.updateButton);

        // Set event listener for the "Update" button
        updateButton.setOnClickListener(v -> {
            String password = newPassword.getText().toString();
            String confirmPasswordStr = confirmPassword.getText().toString();
            if (password.isEmpty() || confirmPasswordStr.isEmpty()) {
                // Password fields are empty, show an error message
                Toast.makeText(context, "Please enter both password and confirmation..", Toast.LENGTH_LONG).show();
            } else if (password.equals(confirmPasswordStr)) {
                // Passwords match, proceed with API call
                changePasswordApiCall(context, dialog, password);
            } else {
                Toast.makeText(context, "Passwords do not match. Please try again.", Toast.LENGTH_LONG).show();
            }
        });
        dialog.show();
    }

    private static void changePasswordApiCall(Context context, Dialog dialog, String password) {
        Call<ResponseBody> call = apiInterface.updatePassword(userId, password);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // Handle successful update
                    Toast.makeText(context, "Password updated successfully!", Toast.LENGTH_LONG).show();
                } else {
                    // Handle error response
                    Toast.makeText(context, "Password update failed. Please try again.", Toast.LENGTH_LONG).show();
                }
                // Dismiss the dialog regardless of success or failure
                dialog.dismiss();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                // Handle API call failure
                Toast.makeText(context, "Network error. Please try again later.", Toast.LENGTH_LONG).show();
                // Dismiss the dialog in case of failure
                dialog.dismiss();
            }
        });
    }
}
