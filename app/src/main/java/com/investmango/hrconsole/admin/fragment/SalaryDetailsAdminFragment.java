package com.investmango.hrconsole.admin.fragment;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.adapter.SalaryDetailAdapter;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.AdminSalaryDetails;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class SalaryDetailsAdminFragment extends Fragment {
    RecyclerView recyclerView;
    List<AdminSalaryDetails> adminSalaryDetails;
    ApiInterface apiInterface;
    private SalaryDetailAdapter adapter;
    LinearLayoutManager manager;
    long userId;
    String token;
    SharedPreferences preferences;
    ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_salary_details_admin, container, false);
        recyclerView = view.findViewById(R.id.salary_details);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();
        recyclerView.setAdapter(adapter);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog); // Initialize the progress dialog
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        getAllSalaryDetails();
        return view;
    }
    private void getAllSalaryDetails() {
        progressDialog.show();
        Call<List<AdminSalaryDetails>> call = apiInterface.getAllSalaryDetails(token);
        call.enqueue(new Callback<List<AdminSalaryDetails>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminSalaryDetails>> call, @NonNull Response<List<AdminSalaryDetails>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    adminSalaryDetails = response.body();
                    if (adminSalaryDetails != null && !adminSalaryDetails.isEmpty()) {
                        adapter = new SalaryDetailAdapter(adminSalaryDetails, getActivity());
                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(getActivity(), "No salary found.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to get meeting details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminSalaryDetails>> call, @NonNull Throwable t) {
                progressDialog.dismiss(); // Dismiss the progress dialog on failure
                Toast.makeText(getActivity(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}