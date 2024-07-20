package com.investmango.hrconsole.manager.activity.fragment;

import android.annotation.SuppressLint;
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

import com.bumptech.glide.Glide;
import com.investmango.hrconsole.EmployeeAction.ViewPagerAdap;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.databinding.FragmentMyPerformanceBinding;
import com.investmango.hrconsole.manager.activity.AchievementFragment;
import com.investmango.hrconsole.manager.activity.PerformanceFragment;
import com.investmango.hrconsole.model.MonthlyPerformanceResp;

import org.json.JSONObject;

import java.time.Month;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyPerformance extends Fragment {
    private ApiInterface apiInterface;
    private long userId;
    String[] languages;
    private String token;
    Month month;
    private SharedPreferences preferences;
    private FragmentMyPerformanceBinding binding;
    static MonthlyPerformanceResp empPerformanceList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_performance, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fetchPerformanceReport();
        setAdapter();
    }

    private void fetchPerformanceReport() {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();
        Call<MonthlyPerformanceResp> call = apiInterface.getMonthPerformance(userId);
        call.enqueue(new Callback<MonthlyPerformanceResp>() {
            @Override
            public void onResponse(@NonNull Call<MonthlyPerformanceResp> call, @NonNull Response<MonthlyPerformanceResp> response) {
                if (response.isSuccessful()) {
                    empPerformanceList = response.body();
                    setUpData();
                    Log.e("performance", "onResponse: " + empPerformanceList);
                } else {
                    Toast.makeText(getContext(), getErrorMessage(response), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<MonthlyPerformanceResp> call, @NonNull Throwable t) {
                Log.e("EmployeePerformance", "Server error", t);
            }
        });
    }

    @SuppressLint("SuspiciousIndentation")
    private void setUpData() {
        Glide.with(getContext()).load(empPerformanceList.getProfilePhoto()).into(binding.profilePhoto);
        binding.department.setText(empPerformanceList.getDepartment());
        binding.name.setText(empPerformanceList.getUserName());
        if (!empPerformanceList.getPendingTaskData().isEmpty())
            binding.task.setText(empPerformanceList.getPendingTaskData().get(0));
    }

    private String getErrorMessage(Response<MonthlyPerformanceResp> response) {
        String errorMessage = "Unknown error";
        try {
            JSONObject errorJson = new JSONObject(response.errorBody().string());
            errorMessage = errorJson.optString("message", errorMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return errorMessage;
    }

    private void setAdapter() {
        ViewPagerAdap adapter = new ViewPagerAdap(getChildFragmentManager(), 0);

        // add fragment to the list
        adapter.addFragment("Performance", new PerformanceFragment());
        adapter.addFragment("Achievements", new AchievementFragment());
        binding.viewPager.setAdapter(adapter);
        binding.tabs.setupWithViewPager(binding.viewPager);

//        binding.tabs.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//            @Override
//            public void onTabSelected(TabLayout.Tab tab) {
//
//                // called when tab selected
//                tab.setText("");
//            }
//
//            @Override
//            public void onTabUnselected(TabLayout.Tab tab) {
//                // called when tab selected
//                tab.setText("hvjbj");
//            }
//
//            @Override
//            public void onTabReselected(TabLayout.Tab tab) {
//
//            }
//
//        });
    }
}