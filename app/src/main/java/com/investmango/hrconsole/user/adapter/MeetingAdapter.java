package com.investmango.hrconsole.user.adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.MeetingDetails;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MeetingAdapter extends RecyclerView.Adapter<MeetingAdapter.LeadsViewHolder> {
    private List<MeetingDetails> meetingDetails;
    private final Context context;
    private ApiInterface apiInterface;
    private SharedPreferences preferences;
    private String token;
    private long userId;

    public MeetingAdapter(List<MeetingDetails> meetingDetails, Context context) {
        this.meetingDetails = meetingDetails;
        this.context = context;

        ApiClient apiClient = new ApiClient(context);
        apiInterface = apiClient.getApiInterface();

        preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
    }

    @NonNull
    @Override
    public LeadsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.meeting_list, viewGroup, false);
        return new LeadsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final LeadsViewHolder leadsViewHolder, int position) {
        MeetingDetails data = meetingDetails.get(position);
        String formattedTime = convertLongToDateString(data.getMeetingTime());
        leadsViewHolder.UserId.setText(String.valueOf(data.getId()));
        leadsViewHolder.Time.setText(formattedTime);
        leadsViewHolder.Purpose.setText(data.getPurpose());
        leadsViewHolder.LocationTextView.setText(data.getLocation());
        String status = data.getStatus();
        if (status.equalsIgnoreCase("SCHEDULED")) {
            status = "Scheduled";
        }
        else if (status.equalsIgnoreCase("POSTPONED")) {
            status = "Postponed";
        }
        else if (status.equalsIgnoreCase("PENDING")) {
            status = "Pending";
        }
        else if (status.equalsIgnoreCase("SUSPENDED")) {
            status = "Suspended";
        }
        else if (status.equalsIgnoreCase("DONE")) {
            status = "Done";
        }
        leadsViewHolder.statusTextView.setText(status);
    }

    @Override
    public int getItemCount() {
        return meetingDetails.size();
    }

    public static class LeadsViewHolder extends RecyclerView.ViewHolder {
        TextView UserId;
        TextView Time;
        TextView Purpose;
        TextView LocationTextView;
        TextView statusTextView;

        public LeadsViewHolder(@NonNull View itemView) {
            super(itemView);
            UserId = itemView.findViewById(R.id.UserId);
            Time = itemView.findViewById(R.id.Time);
            Purpose = itemView.findViewById(R.id.Purpose);
            LocationTextView = itemView.findViewById(R.id.LocationTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
        }
    }
    private String convertLongToDateString(long time) {
        Date date = new Date(time);
        SimpleDateFormat sdf = new SimpleDateFormat(" dd-MMM-yyyy HH:mm a", Locale.getDefault());
        return sdf.format(date);
    }
}
