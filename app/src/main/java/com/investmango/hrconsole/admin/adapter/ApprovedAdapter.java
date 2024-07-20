package com.investmango.hrconsole.admin.adapter;

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
import com.investmango.hrconsole.model.ApprovedLeaves;

import java.util.List;

public class ApprovedAdapter extends RecyclerView.Adapter<ApprovedAdapter.ViewHolder> {
    private final Context context;
    private final List<ApprovedLeaves> leaveList;
    public ApprovedAdapter(Context context, List<ApprovedLeaves> leaveList) {
        this.context = context;
        this.leaveList = leaveList;
    }
    @NonNull
    @Override
    public ApprovedAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.approvedleaves, parent, false);
        return new ApprovedAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ApprovedAdapter.ViewHolder holder, int position) {
        ApprovedLeaves leave = leaveList.get(position);
        // Set the data to the views in the ViewHolder
        holder.empName.setText(capitalizeFirstLetter(leave.getUserName()));
        holder.reasonTexts.setText(leave.getReason());
        String leaveType = leave.getLeaveType();


        switch (leaveType) {
            case "HALF_DAY":
                holder.leaveTypes.setText("Half Day");
                break;
            case "ABSENT":
                holder.leaveTypes.setText("Absent");
                break;
            case "SHORT_LEAVE":
                holder.leaveTypes.setText("Short Leave");
                break;
            case "PAID_LEAVE":
                holder.leaveTypes.setText("Paid Leave");
                break;
            default:
                holder.leaveTypes.setText("Unknown Leave Type");
                break;
        }

        ArrayAdapter<String> datesAdapter = new ArrayAdapter<>(context, R.layout.color_spinner_layout, leave.getLeaveDates());
        datesAdapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        holder.requestDates.setAdapter(datesAdapter);
    }

    private String capitalizeFirstLetter(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    @Override
    public int getItemCount() {
        return leaveList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView empName;
        Spinner requestDates;
        TextView reasonTexts;
        TextView leaveTypes;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            empName = itemView.findViewById(R.id.empName);
            requestDates = itemView.findViewById(R.id.requestDates);
            reasonTexts = itemView.findViewById(R.id.reasonTexts);
            leaveTypes = itemView.findViewById(R.id.leaveTypes);

        }
    }
}
