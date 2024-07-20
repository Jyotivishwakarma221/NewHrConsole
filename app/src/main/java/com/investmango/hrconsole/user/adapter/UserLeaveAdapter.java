package com.investmango.hrconsole.user.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.investmango.hrconsole.R;
import com.investmango.hrconsole.model.SaveUserLeave;

import java.util.List;

public class UserLeaveAdapter extends RecyclerView.Adapter<UserLeaveAdapter.ViewHolder> {
    private final Context context;
    private final List<SaveUserLeave> leaveList;

    public UserLeaveAdapter(Context context, List<SaveUserLeave> leaveList) {
        this.context = context;
        this.leaveList = leaveList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_leave, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        SaveUserLeave leave = leaveList.get(position);
        holder.bind(leave);
    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView leaveIdTextView, reasonTextView, leaveTypeTextView, commentTextView, statusTextView, approvedByTextView;
        Spinner dateSpinner;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            leaveIdTextView = itemView.findViewById(R.id.leave_Id);
            reasonTextView = itemView.findViewById(R.id.reasonText);
            leaveTypeTextView = itemView.findViewById(R.id.leaveType);
            commentTextView = itemView.findViewById(R.id.commentTextView);
            approvedByTextView = itemView.findViewById(R.id.approvedByTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            dateSpinner = itemView.findViewById(R.id.dateSpinner);
        }

        public void bind(SaveUserLeave leave) {
            leaveIdTextView.setText(String.valueOf(leave.getLeaveId()));
            reasonTextView.setText(leave.getReason());
            leaveTypeTextView.setText(formatLeaveType(leave.getLeaveType()));
            commentTextView.setText(leave.getComment());
            approvedByTextView.setText(leave.getApprovedByName());
            statusTextView.setText(formatStatus(leave.getStatus()));
            setupDateSpinner(leave.getLeaveDates());
        }

        private String formatLeaveType(String leaveType) {
            switch (leaveType.toLowerCase()) {
                case "absent":
                    return "Absent";
                case "half_day":
                    return "Half Day";
                case "short_leave":
                    return "Short Leave";
                case "paid_leave":
                    return "Paid Leave";
                default:
                    return leaveType;
            }
        }

        private String formatStatus(String status) {
            switch (status.toLowerCase()) {
                case "pending":
                    return "Pending";
                case "approved":
                    return "Approved";
                case "rejected":
                    return "Rejected";
                default:
                    return status;
            }
        }

        private void setupDateSpinner(List<String> dates) {
            ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(itemView.getContext(), R.layout.color_spinner_layout, dates);
            spinnerAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
            dateSpinner.setAdapter(spinnerAdapter);
        }
    }
}
