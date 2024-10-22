package com.investmango.hrconsole.manager.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.HrConsole.tv.official.console.premium.CommonAdapter;
import com.HrConsole.tv.official.console.premium.RecyclerViewInterface;
import com.investmango.hrconsole.EmployeeAction.AddMeeting;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.databinding.MeetingsOnHomeBinding;
import com.investmango.hrconsole.manager.activity.fragment.Leaves;
import com.investmango.hrconsole.manager.activity.fragment.MyPerformance;
import com.investmango.hrconsole.manager.activity.fragment.PayrollPanel;
import com.investmango.hrconsole.manager.activity.fragment.TasksFragment;
import com.investmango.hrconsole.model.MeetingItem;
import com.investmango.hrconsole.model.MeetingListResponse;
import com.investmango.hrconsole.service.DateAndTimeUtility;
import com.makeramen.roundedimageview.RoundedImageView;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class ManagerFragment extends Fragment implements RecyclerViewInterface<MeetingsOnHomeBinding> {

    String authority;
    long userId;
    SharedPreferences preferences;
    String token;
    List<MeetingItem> meetingLis;
    private ApiInterface apiInterface;
    TextView textViewSeeAll, textViewImportantMeetings;
    RecyclerView meetings;
    List<MeetingItem> meetingList;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = getContext().getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
        authority = preferences.getString("Authority", "user");

        getMeetings();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.manager_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RoundedImageView image = view.findViewById(R.id.managerImageProfile);

        CardView performance = view.findViewById(R.id.checkNowButton);
        textViewSeeAll = view.findViewById(R.id.textViewSeeAll);
        textViewImportantMeetings = view.findViewById(R.id.textViewImportantMeetings);
        CardView message = view.findViewById(R.id.CheckNowMessage);
        CardView MeetingsCard = view.findViewById(R.id.CheckMeeting);
        CardView leaves = view.findViewById(R.id.leavesCheckNowBtn);
        Button fabAttendance = view.findViewById(R.id.fabAttendance);
        CardView CreateAssignmnet = view.findViewById(R.id.CreateAssignmnet);
        CardView assignmet = view.findViewById(R.id.assignments);
        CardView feedbackBtn = view.findViewById(R.id.feedbackBtn);

        ImageView FeedBtn = view.findViewById(R.id.FeedBackBtn);
        ImageView payBtn = view.findViewById(R.id.payBtn);
        ImageView MessgeBtn = view.findViewById(R.id.MessgeBtn);
        ImageView Meetings = view.findViewById(R.id.Meetings);
        ImageView TaskBtn = view.findViewById(R.id.TaskBtn);
        ImageView MyLeaveBtn = view.findViewById(R.id.MyLeaveBtn);
        ImageView AssignMntBtn = view.findViewById(R.id.AssignMntBtn);
        ImageView performanceBtn = view.findViewById(R.id.performanceBtn);

        meetings = view.findViewById(R.id.meetingRecycle);


        CardView payouts = view.findViewById(R.id.checkPayoutsBtn);
        CardView tasks = view.findViewById(R.id.createTaskButton);

        // Set click listeners
        fabAttendance.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment2(new AttendanceFragment(), "ATTENDANCE");
        });

        performance.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new MyPerformance());
        });
        performanceBtn.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new MyPerformance());
        });

        MeetingsCard.setOnClickListener(v -> {
            AddMeeting fragment = new AddMeeting();
            Bundle bb = new Bundle();
            bb.putString("ViewOf", "Own");
            fragment.setArguments(bb);
            ((ManagerActivity) getActivity()).replaceFragment(fragment);
        });

        Meetings.setOnClickListener(v -> {
            AddMeeting fragment = new AddMeeting();
            Bundle bb = new Bundle();
            bb.putString("ViewOf", "Own");
            fragment.setArguments(bb);

            ((ManagerActivity) getActivity()).replaceFragment(fragment);
        });


        textViewSeeAll.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new AddMeeting());
        });

        // Set click listeners
        message.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new MessageFragment());
        });
        MessgeBtn.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new MessageFragment());
        });


        leaves.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new Leaves());
        });
        MyLeaveBtn.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new Leaves());
        });


        payouts.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new PayrollPanel());
        });

        payBtn.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new PayrollPanel());
        });


        tasks.setOnClickListener(v -> {
            TasksFragment fragment = new TasksFragment();
            Bundle bb = new Bundle();
            bb.putString("ViewOf", "Own");
            fragment.setArguments(bb);

            ((ManagerActivity) getActivity()).replaceFragment(fragment);
        });
        TaskBtn.setOnClickListener(v -> {
            TasksFragment fragment = new TasksFragment();
            Bundle bb = new Bundle();
            bb.putString("ViewOf", "Own");
            fragment.setArguments(bb);

            ((ManagerActivity) getActivity()).replaceFragment(fragment);
        });


        feedbackBtn.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new NewFeedBackFragment());
        });
        FeedBtn.setOnClickListener(v -> {
            ((ManagerActivity) getActivity()).replaceFragment(new NewFeedBackFragment());
        });


        CreateAssignmnet.setOnClickListener(v -> {
            AllProjectsFragment fragment = new AllProjectsFragment();
            Bundle bb = new Bundle();
            bb.putString("ViewOf", "Own");
            fragment.setArguments(bb);
            ((ManagerActivity) getActivity()).replaceFragment(fragment);
        });
        AssignMntBtn.setOnClickListener(v -> {
            AllProjectsFragment fragment = new AllProjectsFragment();
            Bundle bb = new Bundle();
            bb.putString("ViewOf", "Own");
            fragment.setArguments(bb);
            ((ManagerActivity) getActivity()).replaceFragment(fragment);
        });

    }

    private void getMeetings() {
        ApiClient apiClient = new ApiClient(getContext());
        apiInterface = apiClient.getApiInterface();

        Call<MeetingListResponse> call = apiInterface.getTodayMeeting(userId);
        call.enqueue(new Callback<MeetingListResponse>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(@NonNull Call<MeetingListResponse> call, @NonNull Response<MeetingListResponse> response) {
                if (response.isSuccessful()) {
                    meetingList = response.body().getContent();
                    Log.e("meetings", String.valueOf(meetingList.size()));
                    if (meetingList != null && !meetingList.isEmpty()) {
                        Instant now = Instant.now();

                        // Convert the instant to LocalDate
                        LocalDate today = now.atZone(ZoneId.systemDefault()).toLocalDate();

                        // Define the desired date format
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

                        // Format the date
                        String formattedDate = today.format(formatter);


//                    Log.e("khushii", "onResponse: "+ meetingList.get(4).getMeetingTime()+ "  "+ formattedDate.toString());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            meetingLis = meetingList.stream()
                                    .filter(name -> DateAndTimeUtility.getDATEFromLong(name.getMeetingTime()).equals(formattedDate))
                                    .collect(Collectors.toList());

                        }

                        if (meetingLis != null && !meetingLis.isEmpty()) {
                            textViewImportantMeetings.setVisibility(View.VISIBLE);
                            textViewSeeAll.setVisibility(View.VISIBLE);
                            setAdapt();
                        }
                    } else {

                        Log.e("getmeetings", "onResponse: " + response.body().toString());
//                        Toast.makeText(getContext(), "No Meetings", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("meetings", "Failed to retrieve meeting user. Response code: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<MeetingListResponse> call, @NonNull Throwable t) {
                Log.e("meetings", "Error retrieving profile: " + t.getMessage());
            }
        });
    }

    void setAdapt() {
        meetings.setAdapter(new CommonAdapter(this));
        meetings.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    @NonNull
    @Override
    public MeetingsOnHomeBinding getViewBinding(@NonNull ViewGroup viewGroup, int viewType) {
        return MeetingsOnHomeBinding.inflate(getLayoutInflater(), viewGroup, false);
    }

    @Override
    public void bindView(@NonNull MeetingsOnHomeBinding viewBind, int position) {
        if (position == 0) {
            viewBind.layout.setBackgroundResource(R.drawable.cardview_border);
        }
        viewBind.description.setText(meetingLis.get(position).getDescription());
        viewBind.title.setText(meetingLis.get(position).getPurpose());
        viewBind.layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((ManagerActivity) getActivity()).replaceFragment(new AddMeeting());

            }
        });
    }


    @Override
    public int getListCount() {
        return meetingLis.size();
    }
}