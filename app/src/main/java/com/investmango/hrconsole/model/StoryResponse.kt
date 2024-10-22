package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class StoryResponse(

	@field:SerializedName("assignedToId")
	val assignedToId: Any? = null,

	@field:SerializedName("updatedTime")
	val updatedTime: Long? = null,

	@field:SerializedName("assignedToName")
	val assignedToName: Any? = null,

	@field:SerializedName("description")
	val description: String? = null,

	@field:SerializedName("priority")
	val priority: Boolean? = null,

	@field:SerializedName("assignmentId")
	val assignmentId: Int? = null,

	@field:SerializedName("priorityLevel")
	val priorityLevel: String? = null,

	@field:SerializedName("assignedByName")
	val assignedByName: String? = null,

	@field:SerializedName("name")
	val name: String? = null,

	@field:SerializedName("createdTime")
	val createdTime: Long? = null,

	@field:SerializedName("fileUrl")
	val fileUrl: String? = null,

	@field:SerializedName("assignmentSubject")
	val assignmentSubject: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("deadline")
	val deadline: Long? = null,

	@field:SerializedName("assignedById")
	val assignedById: Int? = null,

	@field:SerializedName("story")
    var story: List<String?>? = null,

	@field:SerializedName("status")
	val status: String? = null
)
