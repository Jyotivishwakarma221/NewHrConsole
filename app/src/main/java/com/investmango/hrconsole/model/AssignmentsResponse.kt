package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class AssignmentsResponse(

	@field:SerializedName("content")
	val content: List<AssignmentItem?>? = null,
)

data class UsersItem(

	@field:SerializedName("assignToProfile")
	val assignToProfile: String? = null,

	@field:SerializedName("assignToPhone")
	val assignToPhone: String? = null,

	@field:SerializedName("assignToId")
	val assignToId: Long? = null,

	@field:SerializedName("isUserPresent")
	val isUserPresent: Boolean? = null,

	@field:SerializedName("assignToName")
	val assignToName: String? = null,

	@field:SerializedName("assignToEmail")
	val assignToEmail: String? = null,
):Serializable

data class AssignmentItem(

	@field:SerializedName("updatedTime")
	val updatedTime: Long? = null,

	@field:SerializedName("notes")
	val notes: List<String>? = null,

	@field:SerializedName("subject")
	val subject: String? = null,

	@field:SerializedName("userPhone")
	val userPhone: String? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("banner")
	val banner: Any? = null,

	@field:SerializedName("Users")
	val users: List<UsersItem?>? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("deadLine")
	val deadLine: Long? = null,

	@field:SerializedName("userProfile")
	val userProfile: String? = null,

	@field:SerializedName("createdDate")
	val createdDate: Long? = null,

	@field:SerializedName("priorityLevel")
	val priorityLevel: Any? = null,

	@field:SerializedName("stages")
	val stages: Any? = null,

	@field:SerializedName("headings")
	val headings: Any? = null,

	@field:SerializedName("files")
	val files: List<String>? = null,

	@field:SerializedName("comment")
	val comment: Any? = null,

	@field:SerializedName("userEmail")
	val userEmail: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("status")
	val status: String? = null,
) : Serializable
