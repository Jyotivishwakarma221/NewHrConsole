package com.investmango.hrconsole.user.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.MapView;
import com.investmango.hrconsole.R;
import com.investmango.hrconsole.model.Attendance;
import com.investmango.hrconsole.service.CommonUtil;
import com.investmango.hrconsole.service.SharedUtils;

import java.util.List;

public class UserAttendanceHistoryAdapter extends RecyclerView.Adapter<UserAttendanceHistoryAdapter.LeadsViewHolder> {

    private List<Attendance> attendanceList;
    Context context;
    SharedUtils sharedUtils;

    public UserAttendanceHistoryAdapter(List<Attendance> attendanceList, Context context) {
        this.attendanceList = attendanceList;
        this.context = context;
    }

    @NonNull
    @Override
    public LeadsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        sharedUtils = new SharedUtils(context);
        View view = inflater.inflate(R.layout.fragment_user_attendance_history_adapter, viewGroup, false);
        return new LeadsViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull LeadsViewHolder leadsViewHolder, final int i) {
        if (attendanceList.get(i).getDate() != null) {
            leadsViewHolder.attendanceDate.setText(attendanceList.get(i).getDate());
        }
        if (attendanceList.get(i).getInTime() > 0) {
            if (!CommonUtil.isAttendanceOnTime(attendanceList.get(i).getInTime(), "10:00 am", "IN_TIME")) {
                leadsViewHolder.inTime.setBackgroundColor(Color.parseColor("#ab2227"));
            }
            else {
                leadsViewHolder.inTime.setBackgroundColor(Color.parseColor("#ffffff"));
                leadsViewHolder.inTime.setBackgroundResource(R.drawable.attendance_corner_red_background);
            }
            leadsViewHolder.inTime.setText(CommonUtil.getTime(attendanceList.get(i).getInTime()));
        }

        if (attendanceList.get(i).getOutTime() > 0) {
            if (!CommonUtil.isAttendanceOnTime(attendanceList.get(i).getOutTime(), "06:30 pm", "OUT_TIME")) {
                leadsViewHolder.outTime.setBackgroundColor(Color.parseColor("#ab2227"));
            } else {
                leadsViewHolder.outTime.setBackgroundColor(Color.parseColor("#ffffff"));
                leadsViewHolder.outTime.setBackgroundResource(R.drawable.attendance_corner_red_background);
            }
            leadsViewHolder.outTime.setText(CommonUtil.getTime(attendanceList.get(i).getOutTime()));
        }

        if (attendanceList.get(i).getOutTime() > 0) {
            leadsViewHolder.outTime.setText(CommonUtil.getTime(attendanceList.get(i).getOutTime()));
        }
    }

    @Override
    public int getItemCount() {
        return attendanceList.size();
    }
    public static class LeadsViewHolder extends RecyclerView.ViewHolder {
        public MapView inLocationMap;
        public MapView outLocationMap;
        public TextView attendanceDate;
        public TextView inTime;
        public TextView outTime;
        LinearLayout attendanceLinearLayout;

        public LeadsViewHolder(@NonNull View itemView) {
            super(itemView);
            inLocationMap = itemView.findViewById(R.id.inLocationMap);
            outLocationMap = itemView.findViewById(R.id.outLocationMap);
            attendanceDate = itemView.findViewById(R.id.attendanceTime);
            inTime = itemView.findViewById(R.id.inTimeAttendance);
            outTime = itemView.findViewById(R.id.outTimeAttendance);
            attendanceLinearLayout = itemView.findViewById(R.id.userAttendanceLinearLayout);
        }
    }
}


