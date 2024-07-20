package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MeetingListResponse(

	@field:SerializedName("content")
	val content: List<MeetingItem?>? = null
)

data class AssignedUsersItem(

	@field:SerializedName("assignToPhone")
	val assignToPhone: String? = null,

	@field:SerializedName("assignToId")
	val assignToId: Long? = null,

	@field:SerializedName("isUserPresent")
	val isUserPresent: Boolean? = null,

	@field:SerializedName("assignToName")
	val assignToName: String? = null,

	@field:SerializedName("assignToEmail")
	val assignToEmail: String? = null
)

data class MeetingItem(

	@field:SerializedName("updatedTime")
	val updatedTime: Long? = null,

	@field:SerializedName("purpose")
	val purpose: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("assignedUsers")
	val assignedUsers: List<AssignedUsersItem?>? = null,

	@field:SerializedName("assignByPhone")
	val assignByPhone: String? = null,

	@field:SerializedName("meetingTime")
	val meetingTime: Long? = null,

	@field:SerializedName("createdTime")
	val createdTime: Long? = null,

	@field:SerializedName("assignById")
	val assignById: Int? = null,

	@field:SerializedName("location")
	val location: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("assignByEmail")
	val assignByEmail: String? = null,

	@field:SerializedName("assignByName")
	val assignByName: String? = null,

	@field:SerializedName("status")
	val status: String? = null
):Serializable
