package com.spasinnya.domain.model.homework

/**
 * Domain representation of a single question's answer within a lesson.
 *
 * [values] is the list that gets serialized into the `answer_data` TEXT column,
 * e.g. [{"id":"lord","answer":""}] or [{"id":"name","answer":"John"}].
 */
data class HomeworkAnswer(
    val questionId: String,
    val values: List<HomeworkValue>
)

data class HomeworkValue(
    val id: String,
    val answer: String
)
