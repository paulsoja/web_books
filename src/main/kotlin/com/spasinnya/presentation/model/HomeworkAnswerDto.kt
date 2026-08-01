package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeworkAnswerDto(
    val questionId: String,
    val values: List<HomeworkValueDto>
)
