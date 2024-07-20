package com.investmango.hrconsole.model

import com.google.gson.annotations.SerializedName

data class Departments(

	@field:SerializedName("departmentName")
	val departmentName: List<String?>? = null
)
