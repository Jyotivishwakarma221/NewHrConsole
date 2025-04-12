package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class TodayAttendnce(
	@field:SerializedName("serviceRecords")
	val serviceRecords:serviceRecords ?=null ,

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("inLat")
	val inLat: Double? = null,

	@field:SerializedName("outLat")
	val outLat: Double? = null,

	@field:SerializedName("userPhone")
	val userPhone: String? = null,

	@field:SerializedName("outLong")
	val outLong: Double? = null,

	@field:SerializedName("profileImage")
	val profileImage: String? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("inTime")
	val inTime: Long? = null,

	@field:SerializedName("userEmail")
	val userEmail: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("designation")
	val designation: String? = null,

	@field:SerializedName("outTime")
	val outTime: Long? = null,

	@field:SerializedName("inLong")
	val inLong: Double? = null

)

data class serviceRecords(
	@field:SerializedName("id")
	val id: Long,

	@field:SerializedName("createdTime")
	val createdTime: Long,

	val updatedTime: Any?,
	@field:SerializedName("startTime")
	val startTime: Long,
	@field:SerializedName("endTime")
	val endTime: Any?,
	val inLatLong: Any?,
	val outLatLong: Any?,

	@field:SerializedName("reason")
	val reason: String,
	val userId: Long,
	val userName: String,
	val userEmail: String,
	val userPhone: String,
)

