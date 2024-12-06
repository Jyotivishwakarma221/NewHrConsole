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

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.activity.HomeActivity;
import com.investmango.hrconsole.admin.adapter.PreviousTaskAdapter;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.PreviousTask;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class PreviousTaskFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<PreviousTask> previousTask;
    private ApiInterface apiInterface;
    private String token;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_previous, container, false);
        recyclerView = view.findViewById(R.id.previous_taskRecyclerView);

        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog);
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        getPreviousTask();
        return view;
    }
    private void getPreviousTask() {
        progressDialog.show();
        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();
        Call<List<PreviousTask>> call = apiInterface.getPreviousTask();
        call.enqueue(new Callback<List<PreviousTask>>() {
            @Override
            public void onResponse(@NonNull Call<List<PreviousTask>> call, @NonNull Response<List<PreviousTask>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    previousTask = response.body();
                    if (previousTask != null && !previousTask.isEmpty()) {
                        setPreviousTaskAdapter(previousTask);
                    } else {
                        Toast.makeText(getActivity(), "No tasks found.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to get task details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PreviousTask>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Log.e("TaskFragment", "Network error: " + t.getMessage());
                Toast.makeText(getActivity(), "Server Error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    public void setPreviousTaskAdapter(List<PreviousTask> previousTask) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
       PreviousTaskAdapter adapter = new PreviousTaskAdapter(previousTask, getActivity());
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getActivity(), HomeActivity.class);
                startActivity(intent);
                requireActivity().finish();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_UP && keyCode == KeyEvent.KEYCODE_BACK) {
                startActivity(new Intent(getActivity(), HomeActivity.class));
                return true;
            }
            return false;
        });
    }
}