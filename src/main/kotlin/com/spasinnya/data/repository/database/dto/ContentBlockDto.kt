package com.spasinnya.data.repository.database.dto

import com.spasinnya.data.repository.database.table.ContentBlockType
import kotlinx.serialization.Serializable

@Serializable
data class ContentBlockDto(
    val type: ContentBlockType,
    val data: String
)
