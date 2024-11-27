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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.Task;
import com.investmango.hrconsole.user.activity.UserHomeActivity;
import com.investmango.hrconsole.user.adapter.TaskAdapter;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<Task> tasks;
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private SharedPreferences preferences;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;
    private PopupWindow popupWindow;
    private ImageView status;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task, container, false);
        initializeViews(view);
        setupSwipeRefresh();
        setupRecyclerView();
        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
        status = view.findViewById(R.id.filter);
        status.setOnClickListener(v -> showFilterPopup());
        getAllTasks();
        return view;
    }

    private void showFilterPopup() {
        LayoutInflater inflater = (LayoutInflater) requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.filter_task, null);

        int width = LinearLayout.LayoutParams.WRAP_CONTENT;
        int height = LinearLayout.LayoutParams.WRAP_CONTENT;
        boolean focusable = true; // if false, the popup blocks user input
        popupWindow = new PopupWindow(popupView, width, height, focusable);


        TextView doneButton = popupView.findViewById(R.id.done);
        TextView pendingButton = popupView.findViewById(R.id.pending);
        TextView reviewedButton = popupView.findViewById(R.id.reviewed);


        doneButton.setOnClickListener(v -> {
            getFilteredTasks("DONE");
            popupWindow.dismiss();
        });

        pendingButton.setOnClickListener(v -> {
            getFilteredTasks("PENDING");
            popupWindow.dismiss();
        });

        reviewedButton.setOnClickListener(v -> {
            getFilteredTasks("REVIEWED");
            popupWindow.dismiss();
        });

        // Show the popup window
        popupWindow.showAsDropDown(status, 0, 0);
    }


    private void initializeViews(View view) {
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        recyclerView = view.findViewById(R.id.taskRecyclerView);
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
            getAllTasks();
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    private void getFilteredTasks(String taskStatus) {
        ApiClient apiClient = new ApiClient(Objects.requireNonNull(getContext()));
        ApiInterface apiInterface = apiClient.getApiInterface();
        Call<List<Task>> call = apiInterface.getFilterTask( userId, taskStatus);
        call.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(@NonNull Call<List<Task>> call, @NonNull Response<List<Task>> response) {
                if (response.isSuccessful()) {
                    List<Task> filteredTasks = response.body();
                    if (filteredTasks != null) {
                        setTaskAdapter(filteredTasks);
                    } else {
                        Toast.makeText(getActivity(), "No tasks found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to get filtered tasks.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Task>> call, @NonNull Throwable t) {
                Log.e("TaskFragment", "Network error: " + t.getMessage());
                Toast.makeText(getActivity(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getAllTasks() {
        swipeRefreshLayout.setRefreshing(true);
        progressDialog.show();
        ApiClient apiClient = new ApiClient(getActivity());
        ApiInterface apiInterface = apiClient.getApiInterface();
        Call<List<Task>> call = apiInterface.getAllTask(userId);
        call.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(@NonNull Call<List<Task>> call, @NonNull Response<List<Task>> response) {
                swipeRefreshLayout.setRefreshing(false);
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    tasks = response.body();
                    if (tasks != null && !tasks.isEmpty()) {
                        setTaskAdapter(tasks);
                        Log.d("TaskCount", "Task Count: " + tasks.size());
                    } else {
                        Toast.makeText(getActivity(), "No tasks found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to get task details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Task>> call, @NonNull Throwable t) {
                swipeRefreshLayout.setRefreshing(false);
                progressDialog.dismiss();
                Log.e("TaskFragment", "Network error: " + t.getMessage());
                Toast.makeText(getActivity(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setTaskAdapter(List<Task> tasks) {
        TaskAdapter adapter = new TaskAdapter(tasks, getActivity());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isTaskAdded()) {
                    navigateToUserHomeActivity();
                } else {
                    requireActivity().onBackPressed();
                }
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                if (isTaskAdded()) {
                    navigateToUserHomeActivity();
                    return true;
                }
            }
            return false;
        });
    }

    private boolean isTaskAdded() {
        return tasks != null && !tasks.isEmpty();
    }

    private void navigateToUserHomeActivity() {
        Intent intent = new Intent(getActivity(), UserHomeActivity.class);
        startActivity(intent);
        requireActivity().finish();
    }

}
