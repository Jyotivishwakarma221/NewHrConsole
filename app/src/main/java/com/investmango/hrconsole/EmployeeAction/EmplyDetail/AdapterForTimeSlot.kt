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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class AdapterForTimeSlot(
    val fragment: AddMeeting,
    val timeslots: List<String>,
    val meetings: List<MeetingItem?>,
    val userId: Long,
) :
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
//                Log.e("meetings time", "getItemViewType: 1" + timeslots[position])

                for (i in 0..meetings.size - 1) {
//                    Log.e(
//                        "meetings time",
//                        "getItemViewType : " + DateAndTimeUtility.convertEpochToTime(meetings[i]?.meetingTime)
//                    )

                    Log.e("meetings time", "getItemViewType: 2" + timeslots[position])
                    if (DateAndTimeUtility.convertEpochToTime(meetings[i]?.meetingTime)
                            .equals(timeslots[position])
                    ) {

                        for (j in 0..(meetings[i]?.assignedUsers?.size?.minus(1) ?: 0)) {
                            if (meetings[i]?.assignedUsers?.get(j)?.assignToId == userId) {
                                if (meetings[i]?.assignedUsers?.get(j)?.isUserPresent?.equals(false)!!)
                                    holder.acceptMeet.visibility = View.VISIBLE
                                else holder.acceptMeet.visibility = View.GONE
                            }
                        }

                        holder.details.setText(meetings[i]?.purpose)
                        holder.details.setOnClickListener {
                            fragment.EditMeeting(meetings[i]!!)
                        }
                        holder.acceptMeet.setOnClickListener {
                            fragment.AcceptMeet(meetings[i]?.id!!)
                        }
                    }
                }
            }
        }


    }

//    override fun getItemViewType(position: Int): Int {
//        Log.e("checking meeting", "getItemViewType: "+meetings.size )
//        if (meetings != null && meetings.isNotEmpty()) {
//
//            for (meeting in meetings) {
//                Log.e("checking meeting",
//                    DateAndTimeUtility.convertEpochToTime(meeting?.meetingTime) + "  " + timeslots[position]
//                )
//                if (DateAndTimeUtility.convertEpochToTime(meeting?.meetingTime)
//                        .equals(timeslots[position], ignoreCase = true)
//                ) {
//
//                    return 0 // Meeting found
//                }
//            }
//        }
//        return 1
//    }

    override fun getItemViewType(position: Int): Int {
        Log.e("checking meeting", "getItemViewType: " + meetings.size)
        if (meetings != null && meetings.isNotEmpty()) {

            for (meeting in meetings) {
                val meetingTimeStr = DateAndTimeUtility.convertEpochToTime(meeting?.meetingTime)
                Log.e("checking meeting", "$meetingTimeStr  ${timeslots[position]}")

                // Convert the meeting time and timeslot to a Date/Calendar object for easier comparison
                val meetingTime = convertToCalendar(meetingTimeStr)
                val timeslotStart = convertToCalendar(timeslots[position])

                // Check if the meeting time falls within the timeslot
                if (isTimeInSlot(meetingTime, timeslotStart)) {
                    return 0 // Meeting found
                }
            }
        }
        return 1
    }

    // Convert time string (e.g., "5:30") to Calendar object
    fun convertToCalendar(time: String): Calendar {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("h:mm a", Locale.getDefault()) // 12-hour format with AM/PM
        try {
            val date = format.parse(time)
            calendar.time = date
        } catch (e: Exception) {
            Log.e("convertToCalendar", "Error parsing time: $time")
        }
        return calendar
    }

    // Check if the meeting time is within the timeslot (e.g., 5:30 is in 5:00-6:00)
    fun isTimeInSlot(meetingTime: Calendar, slotStart: Calendar): Boolean {
        // Adjust slot start time to match the 1-hour range
        val slotEnd = Calendar.getInstance()
        slotEnd.timeInMillis = slotStart.timeInMillis
        slotEnd.add(Calendar.HOUR_OF_DAY, 1)

        // Check if the meeting time falls within the slot range
        return meetingTime.after(slotStart) && meetingTime.before(slotEnd)
    }


}

sealed class TimeSlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    class NoMeeting(itemView: View) : TimeSlotViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(com.investmango.hrconsole.R.id.Time)
    }

    class
    Meeting(itemView: View) : TimeSlotViewHolder(itemView) {
        val timeTextView: TextView = itemView.findViewById(com.investmango.hrconsole.R.id.Time)
        val details: TextView = itemView.findViewById(com.investmango.hrconsole.R.id.details)
        val acceptMeet: TextView = itemView.findViewById(com.investmango.hrconsole.R.id.acceptMeet)

    }


}
