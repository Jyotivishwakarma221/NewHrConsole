package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class AllFeedResponse(

	@field:SerializedName("content")
	val content: List<FeedbackResponseItem>? = null
)
