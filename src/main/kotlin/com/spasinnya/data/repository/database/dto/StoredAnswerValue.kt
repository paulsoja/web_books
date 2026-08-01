package com.spasinnya.data.repository.database.dto

import kotlinx.serialization.Serializable

/**
 * Storage shape for a single entry inside the `answer_data` TEXT column.
 *
 * The whole `values` list of a question is serialized to a JSON string via
 * [kotlinx.serialization.json.Json.encodeToString] and stored as-is, e.g.:
 * `[{"id":"lord","answer":""}]`.
 */
@Serializable
data class StoredAnswerValue(
    val id: String,
    val answer: String
)
