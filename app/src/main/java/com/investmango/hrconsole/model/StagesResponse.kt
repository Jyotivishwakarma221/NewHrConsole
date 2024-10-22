package com.investmango.hrconsole.model

data class StagesResponse(
	val content: List<Stage>? = null
)

data class Stage(
	val completionTime: String? = null,
	val createdDate: Long? = null,
	val stageName: String? = null,
	val assignmentSubject: String? = null,
	val updatedDate: Long? = null,
	val assignmentsId: Int? = null,
	val stageId: Int? = null,
	val status: String? = null
)

