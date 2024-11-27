package com.investmango.hrconsole.admin.fragment;

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
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.activity.HomeActivity;
import com.investmango.hrconsole.admin.adapter.AdminTaskAdapter;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.AdminTask;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminTaskFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<AdminTask> adminTask;
    private ApiInterface apiInterface;
    private String token;
    private SharedPreferences preferences;
    private SwipeRefreshLayout swipeRefreshLayout;
//    private ImageView previousTaskButton;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_task, container, false);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
//        previousTaskButton = view.findViewById(R.id.previous_Task_Button);
        recyclerView = view.findViewById(R.id.admin_taskRecyclerView);
        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);

        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");

        setupListeners();
        getUserAllTask();
        return view;
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::refreshTasks);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
            refreshTasks();
        });

//        previousTaskButton.setOnClickListener(v -> {
//            PreviousTaskFragment previousTaskFragment = new PreviousTaskFragment();
//            getParentFragmentManager().beginTransaction()
//                    .replace(R.id.fragment_container, previousTaskFragment)
//                    .addToBackStack(null)
//                    .commit();
//        });
    }

    private void refreshTasks() {
        getUserAllTask();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void getUserAllTask() {
        progressDialog.show();
        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();
        Call<List<AdminTask>> call = apiInterface.getUserAllTask();
        call.enqueue(new Callback<List<AdminTask>>() {
            @Override
            public void onResponse(@NonNull Call<List<AdminTask>> call, @NonNull Response<List<AdminTask>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    adminTask = response.body();
                    if (adminTask != null && !adminTask.isEmpty()) {
                        Log.d("TaskFragment", "Total Today Tasks: " + adminTask.size());
                        setAdminTaskAdapter(adminTask);
                    } else {
                        Toast.makeText(getActivity(), "No tasks found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to get task details.", Toast.LENGTH_SHORT).show();
                }
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(@NonNull Call<List<AdminTask>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e("TaskFragment", "Network error: " + t.getMessage());
                Toast.makeText(getActivity(), "Server Error. Please try again.", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void setAdminTaskAdapter(List<AdminTask> adminTask) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        AdminTaskAdapter adapter = new AdminTaskAdapter(adminTask, getActivity());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navigateToHomeActivity();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                navigateToHomeActivity();
                return true;
            }
            return false;
        });
    }

    private void navigateToHomeActivity() {
        Intent intent = new Intent(getActivity(), HomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

}
