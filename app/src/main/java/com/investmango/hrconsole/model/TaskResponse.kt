package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class TaskResponse(

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
	val content: List<TaskItems?>? = null,

	@field:SerializedName("first")
	val first: Boolean? = null,

	@field:SerializedName("totalElements")
	val totalElements: Int? = null,

	@field:SerializedName("empty")
	val empty: Boolean? = null
)

data class TaskItems(

	@field:SerializedName("comments")
	val comments: String? = null,

	@field:SerializedName("subject")
	val subject: String? = null,

	@field:SerializedName("userPhone")
	val userPhone: String? = null,

	@field:SerializedName("createdTime")
	val createdTime: Long? = null,

	@field:SerializedName("fileUrl")
	val fileurl: List<String>? = null,

	@field:SerializedName("userEmail")
	val userEmail: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("updatedDate")
	val updatedDate: Long? = null,

	@field:SerializedName("deadLine")
	val deadLine: Long? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("userId")
	val userId: Int? = null,

	@field:SerializedName("status")
	val status: String? = null
):Serializable
