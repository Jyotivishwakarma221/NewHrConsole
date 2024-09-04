package com.investmango.hrconsole.EmployeeAction.EmplyDetail

import android.R
import android.graphics.drawable.Drawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.investmango.hrconsole.EmployeeAction.AddMeeting
import com.investmango.hrconsole.model.MeetingItem
import com.investmango.hrconsole.service.DateAndTimeUtility
import java.security.AccessController.getContext


class AdapterForTimeSlot(val fragment: AddMeeting,val timeslots: List<String>, val meetings: List<MeetingItem?>) :
    RecyclerView.Adapter<TimeSlotViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeSlotViewHolder {
        if (viewType == 0) {
            val view = LayoutInflater.from(parent.context)
                .inflate(com.investmango.hrconsole.R.layout.meeting_layout1, parent, false)
            return TimeSlotViewHolder.Meeting(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(com.investmango.hrconsole.R.layout.no_meeting_layout, parent, false)
            return TimeSlotViewHolder.NoMeeting(view)
        }
    }

    override fun getItemCount(): Int {
        return timeslots.size
    }

    override fun onBindViewHolder(holder: TimeSlotViewHolder, position: Int) {
        when (holder) {
            is TimeSlotViewHolder.NoMeeting -> {
                holder.timeTextView.text = timeslots[position]

            }

            is TimeSlotViewHolder.Meeting -> {
                holder.timeTextView.text = timeslots[position]

                for (i in 0..meetings.size - 1) {
                    Log.e(
                        "meetings time",
                        "getItemViewType: " + DateAndTimeUtility.convertEpochToTime(meetings[i]?.meetingTime)
                    )
                    Log.e("meetings time", "getItemViewType: " + timeslots[position])
                    if (DateAndTimeUtility.convertEpochToTime(meetings[i]?.meetingTime)
                            .equals(timeslots[position])
                    ) {
                        holder.details.setText(meetings[i]?.purpose)
                        holder.details.setOnClickListener {
                            fragment.EditMeeting(meetings[i]!!)
                        }
                    }
                }
            }
        }


    }

    override fun getItemViewType(position: Int): Int {
        var viewType = 0
        if (meetings != null && !meetings.isEmpty()) {


            for (i in 0..meetings.size - 1) {
                Log.e(
                    "meetings time",
                    "getItemViewType: " + DateAndTimeUtility.convertEpochToTime(meetings[i]?.meetingTime)
                )
//                Log.e("meetings time", "getItemViewType: "+ timeslots[position] )
                if (DateAndTimeUtility.convertEpochToTime(meetings[i]?.meetingTime)
                        .equals(timeslots[position])
                ) {
                    viewType = 0

                } else {

                    viewType = 1
                }
            }
        } else viewType = 1

        return viewType
    }


}

sealed class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    class NoMeeting(itemView: View) : TimeSlotViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(com.investmango.hrconsole.R.id.Time)
    }

    class Meeting(itemView: View) : TimeSlotViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(com.investmango.hrconsole.R.id.Time)
        val details: TextView = itemView.findViewById(com.investmango.hrconsole.R.id.details)
        val acceptMeet: TextView = itemView.findViewById(com.investmango.hrconsole.R.id.acceptMeet)

    }


}
