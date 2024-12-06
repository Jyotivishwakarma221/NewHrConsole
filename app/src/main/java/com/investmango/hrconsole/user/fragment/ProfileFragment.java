package com.investmango.hrconsole.user.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {
    private TextView nameTextView, phoneTextView, dobTextView, genderTextView,
            addressTextView, emailTextView, statusTextView, designationTextView,
            departmentTextView, userNameTextView, permanentsTextView, empIdTextView;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
            refreshProfileList();
        });

        initViews(view);

        SharedPreferences preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "0");
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        getCurrentUser(apiInterface, token);

        progressDialog.show();
        return view;
    }

    private void initViews(View view) {
        empIdTextView = view.findViewById(R.id.empId);
        nameTextView = view.findViewById(R.id.nameTextView);
        phoneTextView = view.findViewById(R.id.phoneTextView);
        dobTextView = view.findViewById(R.id.dobTextView);
        genderTextView = view.findViewById(R.id.genderTextView);
        addressTextView = view.findViewById(R.id.addressTextView);
        emailTextView = view.findViewById(R.id.emailTextView);
        statusTextView = view.findViewById(R.id.statusTextView);
        designationTextView = view.findViewById(R.id.designationTextView);
        departmentTextView = view.findViewById(R.id.departmentTextView);
        userNameTextView = view.findViewById(R.id.userName);
        permanentsTextView = view.findViewById(R.id.permanentsTextView);
    }

    private void refreshProfileList() {
        SharedPreferences preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "0");
        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        getCurrentUser(apiInterface, token);
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getCurrentUser(ApiInterface apiInterface, String token) {
        Call<User> call = apiInterface.getCurrentUser();
        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        populateUserData(user);
                    }
                } else {
                    progressDialog.dismiss();
                    Log.e("ProfileFragment", "Failed to retrieve current user. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e("ProfileFragment", "Error retrieving current user: " + t.getMessage());
            }
        });
    }

    private void populateUserData(User user) {
        String empId = String.valueOf(user.getId());
        String name = capitalize(user.getFirstName()) + " " + capitalize(user.getLastName());
        String dob = formatDOB(user.getDob());
        String address = capitalizeAddress(user.getCurrentAddress());
        String status = user.isEnabled() ? "Active" : "Non-Active";

        empIdTextView.setText(empId);
        nameTextView.setText(name);
        phoneTextView.setText(user.getPhone());
        userNameTextView.setText(user.getUserName());
        dobTextView.setText(dob);
        genderTextView.setText(user.getGender());
        emailTextView.setText(user.getEmail());
        statusTextView.setText(status);
        designationTextView.setText(user.getDesignation());
        departmentTextView.setText(user.getDepartment());
        addressTextView.setText(address);
        permanentsTextView.setText(user.getPermanentAddress());
    }

    private String capitalize(String str) {
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    private String capitalizeAddress(String address) {
        if (address == null) return "";
        StringBuilder sb = new StringBuilder();
        for (String part : address.split(" ")) {
            if (!part.isEmpty()) {
                sb.append(capitalize(part)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String formatDOB(String dob) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
        try {
            Date dobDate = inputFormat.parse(dob);
            if (dobDate != null) {
                return outputFormat.format(dobDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return "";
    }


}
