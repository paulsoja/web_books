package com.spasinnya.domain.repository

import com.spasinnya.domain.model.homework.HomeworkAnswer

interface UserAnswerRepository {

    /**
     * UPSERT each answer by (userId, bookId, questionId).
     * On conflict updates answer_data, week_number, lesson_number and updated_at.
     * Other answers of the user are left untouched.
     */
    suspend fun upsertLessonAnswers(
        userId: Long,
        bookId: String,
        weekNumber: Int,
        lessonNumber: Int,
        answers: List<HomeworkAnswer>
    ): Result<Unit>

    /** Returns only the answers that belong to the given lesson. */
    suspend fun getLessonAnswers(
        userId: Long,
        bookId: String,
        weekNumber: Int,
        lessonNumber: Int
    ): Result<List<HomeworkAnswer>>

    /** Returns a list of lesson numbers that have answers for the given user, book and week. */
    suspend fun getLessonsWithAnswers(
        userId: Long,
        bookId: String,
        weekNumber: Int
    ): Result<List<Int>>
}
