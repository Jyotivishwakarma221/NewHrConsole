package com.investmango.hrconsole.admin.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.adapter.ApprovedAdapter;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.ApprovedLeaves;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ApprovedFragment extends Fragment {
    private RecyclerView recyclerView;
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_approved, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
            refreshApprovedLeaveList();
        });

        SharedPreferences userPreferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = userPreferences.getString("token", "0");
        userId = userPreferences.getLong("userId", 0);

        recyclerView = view.findViewById(R.id.approvedLeavesRecyclerView);
        apiInterface = ApiClient.getClient().create(ApiInterface.class);
        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        getApprovedLeaves();
        return view;
    }

    private void refreshApprovedLeaveList() {
        getApprovedLeaves();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getApprovedLeaves() {
        progressDialog.show();

        // Make the API call with 'token' and 'userId'
        Call<List<ApprovedLeaves>> call = apiInterface.getApprovedLeaves();

        call.enqueue(new Callback<List<ApprovedLeaves>>() {
            @Override
            public void onResponse(@NonNull Call<List<ApprovedLeaves>> call, @NonNull Response<List<ApprovedLeaves>> response) {
                progressDialog.dismiss();
                Log.d("UserLeaveFragment", "Response: " + response.body());

                if (response.isSuccessful()) {
                    List<ApprovedLeaves> leaveList = response.body();
                    if (leaveList != null && !leaveList.isEmpty()) {
                        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                        ApprovedAdapter adapter = new ApprovedAdapter(getActivity(), leaveList);
                        recyclerView.setAdapter(adapter);
                    } else {
                        Log.e("UserLeaveFragment", "User leave is null or empty");
                    }
                } else {
                    Log.e("UserLeaveFragment", "Failed to fetch user leave");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ApprovedLeaves>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e("UserLeaveFragment", "API call failed" + t);
            }
        });
    }
}
