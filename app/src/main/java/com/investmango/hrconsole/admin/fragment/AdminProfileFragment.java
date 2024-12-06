package com.investmango.hrconsole.admin.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.Authority;
import com.investmango.hrconsole.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminProfileFragment extends Fragment {
    private TextView empIdAdmin, admin_UserName, admin_Name, adminPhone, admin_dob, admin_Role, admin_Gender, admin_Email, admin_Status, admin_Address;
    private ProgressDialog progressDialog;
    private ApiInterface apiInterface;
    private String token;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_profile, container, false);

        empIdAdmin = view.findViewById(R.id.empIdAdmin);
        admin_UserName = view.findViewById(R.id.admin_UserName);
        admin_Name = view.findViewById(R.id.admin_Name);
        adminPhone = view.findViewById(R.id.adminPhone);
        admin_dob = view.findViewById(R.id.admin_dob);
        admin_Role = view.findViewById(R.id.admin_Role);
        admin_Gender = view.findViewById(R.id.admin_Gender);
        admin_Email = view.findViewById(R.id.admin_Email);
        admin_Status = view.findViewById(R.id.admin_Status);
        admin_Address = view.findViewById(R.id.admin_Address);

        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        SharedPreferences preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshProfileList);

        getCurrentUser();

        return view;
    }

    private void refreshProfileList() {
        getCurrentUser();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getCurrentUser() {
        progressDialog.show();
        apiInterface.getCurrentUser().enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        updateUI(user);
                    } else {
                        Log.e("AdminProfileFragment", "User data is null");
                    }
                } else {
                    Log.e("AdminProfileFragment", "Failed to fetch user details");
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e("AdminProfileFragment", "API call failed", t);
                progressDialog.dismiss();
            }
        });
    }

    private void updateUI(User user) {
        empIdAdmin.setText(String.valueOf(user.getId()));
        admin_UserName.setText(user.getUserName());
        admin_Name.setText(capitalizeFirstLetter(user.getFirstName()) + " " + capitalizeFirstLetter(user.getLastName()));
        adminPhone.setText(user.getPhone());
        admin_dob.setText(formatDate(user.getDob()));
        admin_Role.setText(getRoles(user.getAuthorities()));
        admin_Gender.setText(capitalizeFirstLetter(user.getGender()));
        admin_Email.setText(user.getEmail());
        admin_Address.setText(user.getCurrentAddress());
        String status = user.isEnabled() ? "Active" : "Non-Active";
        admin_Status.setText(status);
    }

    private String capitalizeFirstLetter(String input) {
        return input.substring(0, 1).toUpperCase(Locale.getDefault()) + input.substring(1);
    }

    private String formatDate(String dateString) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault());
            Date dobDate = inputFormat.parse(dateString);
            return outputFormat.format(dobDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return dateString;
        }
    }

    private String getRoles(List<Authority> authorities) {
        StringBuilder roles = new StringBuilder();
        for (Authority authority : authorities) {
            roles.append(authority.getAuthority()).append(", ");
        }
        return roles.length() > 0 ? roles.substring(0, roles.length() - 2) : "No roles";
    }
}
