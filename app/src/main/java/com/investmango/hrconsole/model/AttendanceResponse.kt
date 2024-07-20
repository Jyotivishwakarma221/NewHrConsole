package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class AttendanceResponse(

	@field:SerializedName("content")
	val content: List<attendanceDay?>? = null,
)

data class attendanceDay(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("leaveType")
	val leaveType: String? = null,

	@field:SerializedName("dayType")
	val dayType: String? = null,

	@field:SerializedName("late")
	val late: Boolean? = null,

	@field:SerializedName("inTime")
	val inTime: Long? = null,

	@field:SerializedName("outLatLong")
	val outLatLong: String? = null,

	@field:SerializedName("userEmail")
	val userEmail: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("designation")
	val designation: String? = null,

	@field:SerializedName("profileImage")
	val profileImage: String? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("outTime")
	val outTime: Long? = null,

	@field:SerializedName("inLatLong")
	val inLatLong: String? = null,
)
