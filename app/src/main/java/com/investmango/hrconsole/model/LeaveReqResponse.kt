package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class LeaveReqResponse(

	@field:SerializedName("number")
	val number: Int? = null,

	@field:SerializedName("last")
	val last: Boolean? = null,

	@field:SerializedName("size")
	val size: Int? = null,

	@field:SerializedName("numberOfElements")
	val numberOfElements: Int? = null,

	@field:SerializedName("totalPages")
	val totalPages: Int? = null,

	@field:SerializedName("pageable")
	val pageable: Pageable? = null,

	@field:SerializedName("sort")
	val sort: Sort? = null,

	@field:SerializedName("content")
	val content: List<ContentItem?>? = null,

	@field:SerializedName("first")
	val first: Boolean? = null,

	@field:SerializedName("totalElements")
	val totalElements: Int? = null,

	@field:SerializedName("empty")
	val empty: Boolean? = null
)


data class ContentItem(

	@field:SerializedName("reason")
	val reason: String? = null,

	@field:SerializedName("userPhone")
	val userPhone: String? = null,

	@field:SerializedName("userprofile")
	val userprofile: String? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("updatedDateTime")
	val updatedDateTime: Long? = null,

	@field:SerializedName("approved")
	val approved: Boolean? = null,

	@field:SerializedName("createdDate")
	val createdDate: Long? = null,

	@field:SerializedName("leaveType")
	val leaveType: String? = null,

	@field:SerializedName("managerStatus")
	val managerStatus: String? = null,

	@field:SerializedName("comment")
	val comment: String? = null,

	@field:SerializedName("fileUrl")
	val fileUrl: String? = null,

	@field:SerializedName("userEmail")
	val userEmail: String? = null,

	@field:SerializedName("approvedByName")
	val approvedByName: Any? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("approvedById")
	val approvedById: Any? = null,

	@field:SerializedName("leaveDates")
	val leaveDates: List<String?>? = null,

	@field:SerializedName("managerUpdateTime")
	val managerUpdateTime: Any? = null,

	@field:SerializedName("OfBycompany")
	val ofBycompany: Any? = null,

	@field:SerializedName("status")
	val status: String? = null
)
