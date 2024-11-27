package com.investmango.hrconsole.admin.fragment;

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
import com.investmango.hrconsole.admin.adapter.TodayPresentEmpAdapter;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.PresentEmployee;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TodayPresentEmployeeFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<PresentEmployee> presentEmployees;
    private ApiInterface apiInterface;
    private String token;
    private SharedPreferences preferences;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_today_present_employee, container, false);
        recyclerView = view.findViewById(R.id.present_employee);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");

        initSwipeRefreshLayout();
        getAllTodayAttendance();
        return view;
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(this::getAllTodayAttendance);
    }

    private void getAllTodayAttendance() {
        swipeRefreshLayout.setRefreshing(true);
        ApiClient apiClient = new ApiClient(requireActivity());
        apiInterface = apiClient.getApiInterface();
        Call<List<PresentEmployee>> call = apiInterface.getAllTodayAttendance();
        call.enqueue(new Callback<List<PresentEmployee>>() {
            @Override
            public void onResponse(@NonNull Call<List<PresentEmployee>> call, @NonNull Response<List<PresentEmployee>> response) {
                swipeRefreshLayout.setRefreshing(false);
                if (response.isSuccessful()) {
                    presentEmployees = response.body();
                    if (presentEmployees != null && !presentEmployees.isEmpty()) {
                        setTodayPresentEmpAdapter(presentEmployees);
                    } else {
                        Toast.makeText(requireActivity(), "No attendance records found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireActivity(), "Failed to get attendance details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PresentEmployee>> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                Log.e("TodayPresentEmployee","Network error: " + t.getMessage());
                Toast.makeText(requireActivity(), "Server Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void setTodayPresentEmpAdapter(List<PresentEmployee> presentEmployees) {
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        TodayPresentEmpAdapter adapter = new TodayPresentEmpAdapter(presentEmployees, requireActivity());
        recyclerView.setAdapter(adapter);
    }

}
