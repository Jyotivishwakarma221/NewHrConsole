package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class BreakMeeting(

	@field:SerializedName("content")
	val breakMeetItem: List<BreakMeetItemItem?>? = null
)

data class BreakMeetItemItem(

	@field:SerializedName("clientName")
	val clientName: String? = null,

	@field:SerializedName("clientEmail")
	val clientEmail: String? = null,

	@field:SerializedName("userPhone")
	val userPhone: String? = null,

	@field:SerializedName("userprofile")
	val userprofile: String? = null,

	@field:SerializedName("updatedDate")
	val updatedDate: Any? = null,

	@field:SerializedName("source")
	val source: String? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("clientAddress")
	val clientAddress: String? = null,

	@field:SerializedName("projectDetails")
	val projectDetails: String? = null,

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("clientPhone")
	val clientPhone: String? = null,

	@field:SerializedName("feedback")
	val feedback: String? = null,

	@field:SerializedName("createdDate")
	val createdDate: Long? = null,

	@field:SerializedName("isDeleted")
	val isDeleted: Boolean? = null,

	@field:SerializedName("channelPartnerName")
	val channelPartnerName: String? = null,

	@field:SerializedName("userEmail")
	val userEmail: String? = null,

	@field:SerializedName("id")
	val id: Int? = null
)
