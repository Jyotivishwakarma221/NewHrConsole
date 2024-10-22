package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class DocumentResponse(

	@field:SerializedName("urls")
	var urls: List<UrlsItem?>? = null,

	@field:SerializedName("createdDate")
	val createdDate: Long? = null,

	@field:SerializedName("userPhone")
	val userPhone: String? = null,

	@field:SerializedName("userEmail")
	val userEmail: String? = null,

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("updatedDate")
	val updatedDate: Int? = null,

	@field:SerializedName("userName")
	val userName: String? = null,

	@field:SerializedName("documentVerified")
	val documentVerified: Boolean? = null,

	@field:SerializedName("userId")
	var userId: Long? = null
)

data class UrlsItem(

	@field:SerializedName("imageUrl")
	val imageUrl: String? = null,

	@field:SerializedName("category")
	val category: String? = null
)
