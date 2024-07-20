package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class MessageResponse(

	@field:SerializedName("content")
	val content: List<messageItem?>? = null,
)

data class messageItem(

	@field:SerializedName("meetingTime")
	val meetingTime: Long? = null,

	@field:SerializedName("image")
	val image: String? = null,

	@field:SerializedName("createdDate")
	val createdDate: Long? = null,

	@field:SerializedName("purpose")
	var purpose: String? = null,

	@field:SerializedName("ChatUserss")
	val chatUserss: List<ChatUserssItem?>? = null,

	@field:SerializedName("userIds")
	var userIds: List<Long?>? = null,

	@field:SerializedName("assignById")
	val assignById: Int? = null,

	@field:SerializedName("id")
	var id: Long? = null,

	@field:SerializedName("assignByEmail")
	val assignByEmail: String? = null,

	@field:SerializedName("message")
	var message: String? = null,

	@field:SerializedName("assignByName")
	val assignByName: String? = null,

	@field:SerializedName("assignByPhone")
	val assignByPhone: String? = null,
)

data class ChatUserssItem(

	@field:SerializedName("assignToPhone")
	val assignToPhone: String? = null,

	@field:SerializedName("assignToId")
	val assignToId: Int? = null,

	@field:SerializedName("assignToName")
	val assignToName: String? = null,

	@field:SerializedName("assignToEmail")
	val assignToEmail: String? = null,
)
