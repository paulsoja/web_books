package com.spasinnya.domain.model.homework

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class WeekHomeworkStatus(
    @SerialName("week_number")
    val weekNumber: Int,
    val completed: Long
)
