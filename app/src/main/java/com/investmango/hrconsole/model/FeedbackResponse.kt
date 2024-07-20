package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class FeedbackResponse(

	val feedbackResponse: List<FeedbackResponseItem?>? = null
)

data class FeedbackResponseItem(

	@field:SerializedName("feedback")
	val feedback: String? = null,

	@field:SerializedName("givenById")
	val givenById: Int? = null,

	@field:SerializedName("createdTime")
	val createdTime: Long? = null,

	@field:SerializedName("section")
	val section: String? = null,

	@field:SerializedName("fileUrl")
	val fileUrl: String? = null,

	@field:SerializedName("givenByPhone")
	val givenByPhone: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("givenByPhoto")
	val givenByPhoto: String? = null,

	@field:SerializedName("status")
	val status: String? = null,

	@field:SerializedName("givenByName")
	val givenByName: String? = null,

	@field:SerializedName("givenByEmail")
	val givenByEmail: String? = null
)
