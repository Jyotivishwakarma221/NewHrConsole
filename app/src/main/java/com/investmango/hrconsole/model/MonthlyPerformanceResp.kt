package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class MonthlyPerformanceResp(

	@field:SerializedName("averagePunchInTime")
	val averagePunchInTime: String? = null,

	@field:SerializedName("presentDays")
	val presentDays: Int? = null,

	@field:SerializedName("monthlyLateCount")
	val monthlyLateCount: Int? = null,

	@field:SerializedName("halfDay")
	val halfDay: Int? = null,

	@field:SerializedName("meetingsSuccessRatio")
	val meetingsSuccessRatio: Double? = null,

	@field:SerializedName("progressData")
	val progressData: List<ProgressDataItem?>? = null,

	@field:SerializedName("totalMeetings")
	val totalMeetings: Int? = null,

	@field:SerializedName("mom")
	val mom: List<MomItem?>? = null,

	@field:SerializedName("absentDays")
	val absentDays: Int? = null,

	@field:SerializedName("overAllPerformace")
	val overAllPerformace: Double? = null,

	@field:SerializedName("completedTasks")
	val completedTasks: Int? = null,

	@field:SerializedName("profilePhoto")
	val profilePhoto: String? = null,

	@field:SerializedName("onTimeTasks")
	val onTimeTasks: Float? = null,

	@field:SerializedName("absent")
	val absent: Int? = null,

	@field:SerializedName("department")
	val department: String? = null,

	@field:SerializedName("pendingTasks")
	val pendingTasks: Int? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("taskCompletionRatio")
	val taskCompletionRatio: Double? = null,

	@field:SerializedName("totalWorkingDays")
	val totalWorkingDays: Int? = null,

	@field:SerializedName("attendanceRatio")
	val attendanceRatio: Double? = null,

	@field:SerializedName("pendingTaskData")
	val pendingTaskData: List<String?>? = null,

	@field:SerializedName("absentMeetings")
	val absentMeetings: Int? = null,

	@field:SerializedName("timeline")
	val timeline: List<TimelineItem?>? = null,

	@field:SerializedName("averageTaskGrowth")
	val averageTaskGrowth: Double? = null,

	@field:SerializedName("presentMeetings")
	val presentMeetings: Int? = null,

	@field:SerializedName("designation")
	val designation: String? = null,

	@field:SerializedName("totalTasks")
	val totalTasks: Int? = null,

	@field:SerializedName("assignedTasks")
	val assignedTasks: Int? = null
)

data class TimelineItem(

	@field:SerializedName("date")
	val date: Long? = null,

	@field:SerializedName("achievement")
	val achievement: String? = null
)

data class MomItem(

	@field:SerializedName("score")
	val score: Double? = null,

	@field:SerializedName("month")
	val month: Long? = null
)

data class ProgressDataItem(

	@field:SerializedName("score")
	val score: Double? = null,

	@field:SerializedName("month")
	val month: Long? = null
)
