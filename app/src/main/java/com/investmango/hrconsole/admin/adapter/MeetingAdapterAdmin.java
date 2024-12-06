package com.investmango.hrconsole.admin.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.fragment.MeetingFragment;
import com.investmango.hrconsole.api.ApiClient;
import com.investmango.hrconsole.api.ApiInterface;
import com.investmango.hrconsole.model.MeetingDetailsAdmin;
import com.investmango.hrconsole.model.UpdateMeeting;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MeetingAdapterAdmin extends RecyclerView.Adapter<MeetingAdapterAdmin.LeadsViewHolder> {
    private final Context context;
    private final List<MeetingDetailsAdmin> meetingDetailsAdmins;
    private final ApiInterface apiInterface;
    private final SharedPreferences preferences;
    private final String token;
    private final long userId;

    public MeetingAdapterAdmin(List<MeetingDetailsAdmin> meetingDetailsAdmins, Context context, MeetingFragment meetingFragment)
    {
        this.meetingDetailsAdmins = meetingDetailsAdmins;
        this.context = context;

        ApiClient apiClient = new ApiClient(context);
        apiInterface = apiClient.getApiInterface();

        preferences = context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE);
        token = preferences.getString("token", "0");
        userId = preferences.getLong("userId", 0);
        Log.d("User ID", "User ID: " + userId);
    }

    @NonNull
    @Override
    public LeadsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.meeting_list2, parent, false);
        return new LeadsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeetingAdapterAdmin.LeadsViewHolder holder, int position) {
        MeetingDetailsAdmin data = meetingDetailsAdmins.get(position);

        holder.User_ID.setText(String.valueOf(data.getId()));
        holder.TimeTextView.setText(convertTimestampToReadableFormat(data.getMeetingTime()));
        holder.PurposeTextVew.setText(data.getPurpose());
        holder.Location.setText(data.getLocation());
        holder.Description.setText(data.getDescription());

        setupSpinner(holder, data.getId(), holder.getAdapterPosition(),formatStatus(data.getStatus()));

        holder.assignUser.setOnClickListener(v ->
        {
            if (data.getAssignedUsers() != null && !data.getAssignedUsers().isEmpty()) {
                StringBuilder assignedNames = new StringBuilder();
                for (MeetingDetailsAdmin.AssignedUser assignedUser : data.getAssignedUsers()) {
                    String userName = assignedUser.getAssignToName();
                    if (!userName.isEmpty()) {
                        userName = Character.toUpperCase(userName.charAt(0)) + userName.substring(1);
                    }
                    assignedNames.append(userName).append("\n"); // Append newline character
                }
                if (assignedNames.length() > 0) {
                    assignedNames.deleteCharAt(assignedNames.length() - 1);
                }
                showAssignUserDialog(context, assignedNames.toString());
            } else {
                showAssignUserDialog(context, "");
            }
        });
    }

    private void showAssignUserDialog(Context context, String assignedNamesString) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        View view = LayoutInflater.from(context).inflate(R.layout.assign_user_dialog_layout, null);
        builder.setView(view);

        TextView assignedUserTextView = view.findViewById(R.id.assignedUser);
        assignedUserTextView.setText(assignedNamesString);
        // Create the dialog
        AlertDialog dialog = builder.create();

        // Set the width of the dialog's window based on the text length
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
        layoutParams.copyFrom(Objects.requireNonNull(dialog.getWindow()).getAttributes());
        layoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        dialog.getWindow().setAttributes(layoutParams);

        dialog.show();
    }


    private String convertTimestampToReadableFormat(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy HH:mm a", Locale.getDefault());
        return sdf.format(new Date(timestamp));
    }

    private String formatStatus(String status) {
        switch (status.toLowerCase()) {
            case "postponed":
                return "Postponed";
            case "done":
                return "Done";
            case "scheduled":
                return "Scheduled";
            case "suspended":
                return "Suspended";
            case "pending":
                return "Pending";
            default:
                return status;
        }
    }

    private void setupSpinner(@NonNull MeetingAdapterAdmin.LeadsViewHolder holder, long meetingId, int adapterPosition, String status) {
        List<String> list = new ArrayList<>();
        list.add(formatStatus(status));
        list.add("Select");
        list.add("Scheduled");
        list.add("Postponed");
        list.add("Pending");
        list.add("Suspended");
        list.add("Done");

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(context, R.layout.color_spinner_layout, list);
        arrayAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        holder.spinner.setAdapter(arrayAdapter);

        holder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int i, long id) {
                holder.spinner.setSelection(i);
                if (i > 1) {
                    UpdateMeeting.Status selectedStatus;
                    // Adjust status based on selected item position
                    switch (i) {
                        case 2:
                            selectedStatus = UpdateMeeting.Status.SCHEDULED;
                            break;
                        case 3:
                            selectedStatus = UpdateMeeting.Status.POSTPONED;
                            break;
                        case 4:
                            selectedStatus = UpdateMeeting.Status.PENDING;
                            break;
                        case 5:
                            selectedStatus = UpdateMeeting.Status.SUSPENDED;
                            break;
                        case 6:
                            selectedStatus = UpdateMeeting.Status.DONE;
                            break;
                        default:
                            selectedStatus = null;
                            break;
                    }

                    UpdateMeeting requestBody = new UpdateMeeting();
                    requestBody.setId(meetingId);
                    requestBody.setStatus(selectedStatus);

                    Call<ResponseBody> call = apiInterface.updateAdminMeetingStatus(requestBody, userId);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "Status updated successfully!", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(context, "Failed to update status.", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                            // Handle API call failure
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Handle nothing selected
            }
        });
    }



    @Override
    public int getItemCount() {
        return meetingDetailsAdmins.size();
    }

    public static class LeadsViewHolder extends RecyclerView.ViewHolder {
        TextView User_ID, TimeTextView, PurposeTextVew, Location, Description,assignedUserTextView;
        Spinner spinner;
        ImageView assignUser;

        public LeadsViewHolder(@NonNull View itemView) {
            super(itemView);
            User_ID = itemView.findViewById(R.id.User_ID);
            TimeTextView = itemView.findViewById(R.id.TimeTextView);
            PurposeTextVew = itemView.findViewById(R.id.PurposeTextVew);
            Location = itemView.findViewById(R.id.Location);
            assignUser = itemView.findViewById(R.id.assignUser);
            Description = itemView.findViewById(R.id.Description);
            spinner = itemView.findViewById(R.id.spinner);
            assignedUserTextView = itemView.findViewById(R.id.assignedUser);
        }
    }

}