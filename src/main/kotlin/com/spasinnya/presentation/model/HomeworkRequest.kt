package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class HomeworkRequest(
    val answers: List<HomeworkAnswerDto>
)
