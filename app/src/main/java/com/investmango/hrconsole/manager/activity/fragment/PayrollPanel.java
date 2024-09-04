package com.investmango.hrconsole.manager.activity.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.databinding.FragmentPayrollPanelBinding;
import com.investmango.hrconsole.model.Salary;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PayrollPanel extends Fragment {
    private ApiInterface apiInterface;
    private String token;
    private Long userId;
    private SharedPreferences preferences;

    Salary salary;
    FragmentPayrollPanelBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(getLayoutInflater(), R.layout.fragment_payroll_panel, container, false);
        return binding.getRoot();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = getContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        fetchSalaryAndOpenFragment();
    }

    private void fetchSalaryAndOpenFragment() {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();
        Call<List<Salary>> call = apiInterface.userSalary(token, userId);
        call.enqueue(new Callback<List<Salary>>() {
            @Override
            public void onResponse(@NonNull Call<List<Salary>> call, @NonNull Response<List<Salary>> response) {
                if (response.isSuccessful()) {
                    List<Salary> salaries = response.body();
                    if (salaries != null && !salaries.isEmpty()) {
                        salary = salaries.get(0);
                        setData();

                    } else {
                        Toast.makeText(getContext(), "No payroll found.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Salary>> call, @NonNull Throwable t) {
                Log.e("SalaryFragment", "Network error: " + t.getMessage());
                if (isAdded())
                Toast.makeText(getContext(), "No payroll found .", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setData() {
        try {
            binding.id.setText(String.valueOf(salary.getId()));
            binding.basicSalary.setText(String.valueOf(salary.getBasicSalary()));
            binding.hra.setText(String.valueOf(salary.getHra()));
            binding.bonus.setText(String.valueOf(salary.getBonus()));
            binding.incentive.setText(String.valueOf(salary.getIncentive()));
            binding.medicalfund.setText(String.valueOf(salary.getMedicalFund()));
            binding.name.setText(salary.getUserName());
            binding.totalSalary.setText(String.valueOf(salary.getTotalSalary()));
            binding.deduction.setText(String.valueOf(salary.getDeduction()));
        } catch (Exception e) {
            Log.e("Exception", "setData: " + e);
            Toast.makeText(getContext(), "Something went wrong.", Toast.LENGTH_SHORT).show();
        }

    }

}