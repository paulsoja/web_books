package com.spasinnya.data.repository.database.dto

import com.spasinnya.data.repository.database.table.HomeworkComponent
import kotlinx.serialization.Serializable

@Serializable
data class HomeworkBlockDto(
    val id: Int? = null,
    val component: HomeworkComponent,
    val answer: String? = null,
    val text: String? = null
)
