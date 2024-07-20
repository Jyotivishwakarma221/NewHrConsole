package com.investmango.hrconsole.admin.adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.admin.fragment.ApproveDialogFragment;
import com.investmango.hrconsole.model.LeaveRequest;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class LeaveRequestAdapter extends RecyclerView.Adapter<LeaveRequestAdapter.LeaveRequestViewHolder> {
    private final List<LeaveRequest> leaveRequestsList;
    private Context context;

    public LeaveRequestAdapter(Context context, List<LeaveRequest> leaveRequestsList) {
        this.context = context;
        this.leaveRequestsList = leaveRequestsList;
    }

    @NonNull
    @Override
    public LeaveRequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.leave_request, parent, false);
        return new LeaveRequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LeaveRequestViewHolder holder, int position) {
        LeaveRequest leaveRequest = leaveRequestsList.get(position);
        holder.created_date.setText(formatTimestamp(leaveRequest.getCreatedDate()));
        holder.Id.setText(String.valueOf(leaveRequest.getId()));
        // Convert the first letter of the username to capital
        String userName = leaveRequest.getUserName();
        if (!TextUtils.isEmpty(userName)) {
            userName = userName.substring(0, 1).toUpperCase() + userName.substring(1);
        }
        holder.naam.setText(userName);
        holder.reason.setText(leaveRequest.getReason());
        List<String> leaveDates = leaveRequest.getLeaveDates();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, R.layout.color_spinner_layout, leaveDates);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        holder.request_date.setAdapter(adapter);

        // Convert the leave type to the desired format
        String leaveType = leaveRequest.getLeaveType();
        if (leaveType.equalsIgnoreCase("ABSENT")) {
            leaveType = "Absent";
        }
        else if (leaveType.equalsIgnoreCase("HALF_DAY")) {
            leaveType = "Half Day";
        }
        else if (leaveType.equalsIgnoreCase("SHORT_LEAVE")) {
            leaveType = "Short Leave";
        }
        else if (leaveType.equalsIgnoreCase("PAID_LEAVE")) {
            leaveType = "Paid Leave";
        }
        holder.leave_Type.setText(leaveType);

        holder.ApproveBtn.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            ApproveDialogFragment dialogFragment = new ApproveDialogFragment();
            dialogFragment.setId(leaveRequest.getId());
            dialogFragment.setUserId(leaveRequest.getUserId());
            dialogFragment.show(fragmentManager, "ApproveDialog");
        });
    }

    @Override
    public int getItemCount() {
        return leaveRequestsList != null ? leaveRequestsList.size() : 0;
    }

    public void updateLeaveList(List<LeaveRequest> newList) {
        leaveRequestsList.clear();
        leaveRequestsList.addAll(newList);
        notifyDataSetChanged();
    }

    public static class LeaveRequestViewHolder extends RecyclerView.ViewHolder {
        TextView Id;
        TextView naam;
        TextView reason;
        Spinner request_date;
        TextView leave_Type;
        TextView created_date;
        Button ApproveBtn;

        public LeaveRequestViewHolder(@NonNull View itemView) {
            super(itemView);
            Id = itemView.findViewById(R.id.Id);
            created_date = itemView.findViewById(R.id.created_date);
            naam = itemView.findViewById(R.id.naam);
            reason = itemView.findViewById(R.id.reason);
            request_date = itemView.findViewById(R.id.request_date);
            leave_Type = itemView.findViewById(R.id.leave_Type);
            ApproveBtn = itemView.findViewById(R.id.ApproveBtn);

            reason.setOnClickListener(v -> showReasonPopup(reason.getContext(), reason.getText().toString()));

            
        }
        private AlertDialog dialog;
        private void showReasonPopup(Context context, String reasonText) {
            if (context == null) {
                Log.e("LeaveRequestAdapter", "Context is null");
                return;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            View view = LayoutInflater.from(context).inflate(R.layout.reason_popup_dialog, null);
            if (view == null) {
                Log.e("LeaveRequestAdapter", "Failed to inflate layout");
                return;
            }
            builder.setView(view);

            TextView popupReasonTextView = view.findViewById(R.id.popup_reason);
            popupReasonTextView.setText(reasonText);

            ImageView closeButton = view.findViewById(R.id.close_button);
            closeButton.setOnClickListener(v -> {
                // Dismiss the dialog when the close button is clicked
                if (dialog != null) {
                    dialog.dismiss();
                } else {
                    Log.e("LeaveRequestAdapter", "Dialog is null");
                }
            });

            dialog = builder.create();
            if (dialog != null) {
                dialog.show();
            } else {
                Log.e("LeaveRequestAdapter", "Failed to create dialog");
            }
        }



    }
    private String formatTimestamp(long timestamp) {
        DateTimeFormatter formatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");
        }
        ZonedDateTime zonedDateTime = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            zonedDateTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return zonedDateTime.format(formatter);
        }
        return null;
    }

}


