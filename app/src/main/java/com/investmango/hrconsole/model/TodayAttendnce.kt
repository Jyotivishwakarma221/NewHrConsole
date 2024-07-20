package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class TodayAttendnce(

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
