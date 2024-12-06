package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class AddSubTask(

	@field:SerializedName("taskDeadline")
	var taskDeadline: Long? = null,

	@field:SerializedName("priorityLevel")
	var priorityLevel: String? = null,

	@field:SerializedName("assignToId")
	var assignToId: Long? = null,

	@field:SerializedName("subtaskDescription")
	var subtaskDescription: String? = null,

	@field:SerializedName("subtaskStatus")
	var subtaskStatus: String? = null,

	@field:SerializedName("assignById")
	var assignById: Long? = null,

	@field:SerializedName("subtaskName")
	var subtaskName: String? = null,

	@field:SerializedName("fileurl")
	var fileurl: List<String>? = null,

	@field:SerializedName("id")
	var id: Int? = null,

	@field:SerializedName("priority")
	var priority: Boolean? = null,

	@field:SerializedName("story")
	var story: List<String?>? = null
)
