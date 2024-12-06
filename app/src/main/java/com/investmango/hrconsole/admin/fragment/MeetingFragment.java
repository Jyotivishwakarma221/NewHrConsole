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
import com.investmango.hrconsole.admin.adapter.MeetingAdapterAdmin;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.MeetingDetailsAdmin;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingFragment extends Fragment {
    RecyclerView recyclerView;
    List<MeetingDetailsAdmin> meetingDetailsAdmins;
    ApiInterface apiInterface;
    private MeetingAdapterAdmin adapter;
    LinearLayoutManager manager;
    long userId;
    String token;
    SharedPreferences preferences;
    ProgressDialog progressDialog;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_meeting2, container, false);
        recyclerView = view.findViewById(R.id.admin_listRecyclerView);
        manager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(adapter);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getActivity(), "Refreshing", Toast.LENGTH_SHORT).show();
            getAllMeeting();
            swipeRefreshLayout.setRefreshing(false);
        });

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();
        recyclerView.setAdapter(adapter);
        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog); // Initialize the progress dialog
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);
        getAllMeeting();
        return view;
    }
    private void getAllMeeting() {
        progressDialog.show();
        Call<List<MeetingDetailsAdmin>> call = apiInterface.getAllMeeting( userId);
        call.enqueue(new Callback<List<MeetingDetailsAdmin>>() {
            @Override
            public void onResponse(@NonNull Call<List<MeetingDetailsAdmin>> call, @NonNull Response<List<MeetingDetailsAdmin>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    meetingDetailsAdmins = response.body();
                    if (meetingDetailsAdmins != null && !meetingDetailsAdmins.isEmpty()) {
                        adapter = new MeetingAdapterAdmin(meetingDetailsAdmins, getActivity(), MeetingFragment.this);
                        // Get the meeting count
                        int meetingCount = meetingDetailsAdmins.size();

                        // Convert the meeting count to a string
                        String meetingCountString = Integer.toString(meetingCount);

                        // Use the meetingCountString as needed
                        Log.d("MeetingCount", "Meeting Count: " + meetingCountString);

                        recyclerView.setAdapter(adapter);
                    } else {
                        Toast.makeText(getActivity(), "No meetings found.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to get meeting details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MeetingDetailsAdmin>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
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