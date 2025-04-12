package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class BreaksListResponse(

	@field:SerializedName("content")
	val content: List<breakItem?>? = null
)

data class breakItem(

	@field:SerializedName("outLatLong")
	val outLatLong: Any? = null,

	@field:SerializedName("updatedTime")
	val updatedTime: Long? = null,

	@field:SerializedName("reason")
	val reason: String? = null,

	@field:SerializedName("userPhone")
	val userPhone: String? = null,

	@field:SerializedName("createdTime")
	val createdTime: Long? = null,

	@field:SerializedName("startTime")
	val startTime: Long? = null,

	@field:SerializedName("userEmail")
	val userEmail: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("endTime")
	val endTime: Long? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("inLatLong")
	val inLatLong: Any? = null
)
