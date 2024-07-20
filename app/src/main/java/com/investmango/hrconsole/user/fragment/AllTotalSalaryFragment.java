package com.investmango.hrconsole.user.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.AllSalaryDetail;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllTotalSalaryFragment extends DialogFragment {
    private ProgressDialog progressDialog;
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private SharedPreferences preferences;
    private TextView user_Id;
    private TextView full_Name;
    private TextView taken_Amount;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        Dialog dialog = new Dialog(requireContext(), R.style.DialogTheme);
        View view = LayoutInflater.from(requireContext()).inflate(R.layout.fragment_all_total_salary, null);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(view);

        user_Id = view.findViewById(R.id.user_Id);
        full_Name = view.findViewById(R.id.full_Name);
        taken_Amount = view.findViewById(R.id.taken_Amount);

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();

        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogAnimation);
            WindowManager.LayoutParams layoutParams = window.getAttributes();
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.dimAmount = 0.8f;

            window.setAttributes(layoutParams);
        }

        allSalaryTaken(token, userId);
        return dialog;
    }

    private void allSalaryTaken(String token, long userId) {
        progressDialog.show();
        Call<AllSalaryDetail> call = apiInterface.allSalaryTaken(token, userId);
        call.enqueue(new Callback<AllSalaryDetail>() {
            @Override
            public void onResponse(@NonNull Call<AllSalaryDetail> call, @NonNull Response<AllSalaryDetail> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    AllSalaryDetail salary = response.body();
                    if (salary != null) {
                        String userName = salary.getUserName();
                        if (userName != null && !userName.isEmpty()) {
                            userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
                        }
                        user_Id.setText(String.valueOf(salary.getUserId()));
                        full_Name.setText(userName);
                        taken_Amount.setText(String.valueOf(salary.getTakenAmount()));
                    } else {
                        Log.e("AllTotalSalaryFragment", "Salary data is null");
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<AllSalaryDetail> call, @NonNull Throwable t) {
                Log.e("AllTotalSalaryFragment", "API call failed", t);
                progressDialog.dismiss();
            }
        });
    }
}
