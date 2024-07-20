package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class MonthlyPerformanceResp(

	@field:SerializedName("pendingTasks")
	val pendingTasks: Int? = null,

	@field:SerializedName("presentDays")
	val presentDays: Int? = null,

	@field:SerializedName("halfDay")
	val halfDay: Int? = null,

	@field:SerializedName("meetingsSuccessRatio")
	val meetingsSuccessRatio: Int? = null,

	@field:SerializedName("totalMeetings")
	val totalMeetings: Int? = null,

	@field:SerializedName("absentDays")
	val absentDays: Int? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("taskCompletionRatio")
	val taskCompletionRatio: Double? = null,

	@field:SerializedName("totalWorkingDays")
	val totalWorkingDays: Int? = null,

	@field:SerializedName("attendanceRatio")
	val attendanceRatio: Double? = null,

	@field:SerializedName("overAllPerformace")
	val overAllPerformace: Double? = null,

	@field:SerializedName("completedTasks")
	val completedTasks: Int? = null,

	@field:SerializedName("profilePhoto")
	val profilePhoto: String? = null,

	@field:SerializedName("pendingTaskData")
	val pendingTaskData: List<String?>? = null,

	@field:SerializedName("onTimeTasks")
	val onTimeTasks: Double? = null,

	@field:SerializedName("absentMeetings")
	val absentMeetings: Int? = null,

	@field:SerializedName("absent")
	val absent: Int? = null,

	@field:SerializedName("presentMeetings")
	val presentMeetings: Int? = null,

	@field:SerializedName("totalTasks")
	val totalTasks: Int? = null,

	@field:SerializedName("department")
	val department: String? = null,

	@field:SerializedName("assignedTasks")
	val assignedTasks: Int? = null,
)
