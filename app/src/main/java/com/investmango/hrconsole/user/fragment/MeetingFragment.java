package com.investmango.hrconsole.user.fragment;

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
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.MeetingDetails;
import com.investmango.hrconsole.user.adapter.MeetingAdapter;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingFragment extends Fragment {
    private RecyclerView recyclerView;
    private List<MeetingDetails> meetingDetails;
    private ApiInterface apiInterface;
    private long userId;
    private String token;
    private SharedPreferences preferences;
    private ProgressDialog progressDialog;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_meeting, container, false);
        recyclerView = view.findViewById(R.id.listRecyclerView);

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(this::refreshMeetingList);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            Toast.makeText(getActivity(), "Refreshing...", Toast.LENGTH_SHORT).show();
            refreshMeetingList();
        });

        ApiClient apiClient = new ApiClient(getActivity());
        apiInterface = apiClient.getApiInterface();

        preferences = requireActivity().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);

        progressDialog = new ProgressDialog(getActivity(), R.style.CustomProgressDialog); // Initialize the progress dialog
        progressDialog.setMessage("Please wait");
        progressDialog.setCancelable(false);

        getAllTodayMeeting();
        return view;
    }
    private void refreshMeetingList() {
        getAllTodayMeeting();
        swipeRefreshLayout.setRefreshing(false);
    }
    private void getAllTodayMeeting() {
        progressDialog.show();
        Call<List<MeetingDetails>> call = apiInterface.getAllTodayMeeting(token, userId);
        call.enqueue(new Callback<List<MeetingDetails>>() {
            @Override
            public void onResponse(@NonNull Call<List<MeetingDetails>> call, @NonNull Response<List<MeetingDetails>> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    meetingDetails = response.body();
                    if (meetingDetails != null && !meetingDetails.isEmpty()) {
                        setMeetingAdapter(meetingDetails);
                        int meetingCount = meetingDetails.size();
                        String meetingCountString = Integer.toString(meetingCount);
                        Log.d("MeetingCount", "Meeting Count: " + meetingCountString);
                    } else {
                        Toast.makeText(getActivity(), "No meetings found.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getActivity(), "Failed to get meeting details.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<MeetingDetails>> call, @NonNull Throwable t) {
                progressDialog.dismiss();
                Toast.makeText(getActivity(), "Network error. Please try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setMeetingAdapter(List<MeetingDetails> meetingDetails) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        MeetingAdapter adapter = new MeetingAdapter(meetingDetails, getActivity());
        recyclerView.setAdapter(adapter);
    }

}
