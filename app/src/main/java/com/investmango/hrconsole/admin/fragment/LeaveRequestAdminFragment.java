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
import com.investmango.hrconsole.admin.adapter.LeaveRequestAdapter;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.LeaveRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveRequestAdminFragment extends Fragment {
    RecyclerView recyclerView;
    List<LeaveRequest> leaveRequest;
    ApiInterface apiInterface;
    private LeaveRequestAdapter adapter;
    LinearLayoutManager manager;
    long userId;
    String token;
    SharedPreferences preferences;
    ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_leave_request_admin, container, false);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
            // Call the method to refresh the data
            refreshLeaveRequests();
        });

        recyclerView = view.findViewById(R.id.leave_Request_Recycler);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();

        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        // Initialize the adapter with an empty list
        leaveRequest = new ArrayList<>();
        adapter = new LeaveRequestAdapter(getActivity(), leaveRequest);
        recyclerView.setAdapter(adapter);

        getAllPendingLeave();

        return view;
    }

    private void refreshLeaveRequests() {
        getAllPendingLeave();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getAllPendingLeave() {
        progressDialog.show();

        Call<List<LeaveRequest>> call = apiInterface.getAllPendingLeave(token);
        call.enqueue(new Callback<List<LeaveRequest>>() {
            @Override
            public void onResponse(@NonNull Call<List<LeaveRequest>> call, @NonNull Response<List<LeaveRequest>> response) {
                progressDialog.dismiss();

                List<LeaveRequest> leaveList1 = response.body();
                if (leaveList1 != null && !leaveList1.isEmpty()) {
                    adapter.updateLeaveList(leaveList1);
                } else {
                    Log.e("LeaveRequestFragment", "Leave list is null or empty");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LeaveRequest>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
