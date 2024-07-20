package com.investmango.hrconsole.user.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.SaveUserLeave;
import com.investmango.hrconsole.user.activity.UserHomeActivity;
import com.investmango.hrconsole.user.adapter.UserLeaveAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserLeaveFragment extends Fragment {
    private RecyclerView recyclerView;
    private ApiInterface apiInterface;
    private List<SaveUserLeave> saveUserLeaves;
    private long userId;
    private String token;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_leave, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshUserLeaveList);

        SharedPreferences userPreferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = userPreferences.getString("token", "0");
        userId = userPreferences.getLong("userId", 0);

        recyclerView = view.findViewById(R.id.userLeaveRecyclerView);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Loading..");
        progressDialog.setCancelable(false);

        getUserLeave();
        return view;
    }

    private void refreshUserLeaveList() {
        getUserLeave();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getUserLeave() {
        progressDialog.show();
        Call<List<SaveUserLeave>> call = apiInterface.getUserLeave(token, userId);
        call.enqueue(new Callback<List<SaveUserLeave>>() {
            @Override
            public void onResponse(@NonNull Call<List<SaveUserLeave>> call, @NonNull Response<List<SaveUserLeave>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    List<SaveUserLeave> leaveList = response.body();
                    if (leaveList != null && !leaveList.isEmpty()) {
                        formatLeaveDates(leaveList);
                        setupRecyclerView(leaveList);
                        Log.d("UserLeaveCount", "User Leave Count: " + leaveList.size());
                    } else {
                        Log.e("UserLeaveFragment", "User leave is null or empty");
                    }
                } else {
                    Log.e("UserLeaveFragment", "Failed to fetch user leave");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<SaveUserLeave>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e("UserLeaveFragment", "API call failed" + t);
            }
        });
    }

    private void formatLeaveDates(List<SaveUserLeave> leaveList) {
        for (SaveUserLeave leave : leaveList) {
            List<String> dateList = leave.getLeaveDates();
            StringBuilder datesStringBuilder = new StringBuilder();
            for (String date : dateList) {
                String formattedDate = date.replaceAll("-", "-");
                // Append each date in a new line
                datesStringBuilder.append(formattedDate).append("\n");
            }
            // Remove the last newline character
            String allDates = datesStringBuilder.toString().trim();
            leave.setFormattedDate(allDates);
        }
    }

    private void setupRecyclerView(List<SaveUserLeave> leaveList) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        UserLeaveAdapter adapter = new UserLeaveAdapter(getActivity(), leaveList);
        recyclerView.setAdapter(adapter);
    }


    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToUserHomeActivity();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                navigateToUserHomeActivity();
                return true;
            }
            return false;
        });
    }

    private void navigateToUserHomeActivity() {
        Intent intent = new Intent(getActivity(), UserHomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

}
