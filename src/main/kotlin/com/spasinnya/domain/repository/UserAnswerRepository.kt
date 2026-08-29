package com.spasinnya.domain.repository

import com.spasinnya.domain.model.homework.BookHomeworkStatus
import com.spasinnya.domain.model.homework.HomeworkAnswer
import com.spasinnya.domain.model.homework.WeekHomeworkStatus

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

    /** Returns count of answers per week for the given user and book. */
    suspend fun getHomeworkStatusByBookId(
        userId: Long,
        bookId: String
    ): Result<List<WeekHomeworkStatus>>

    /** Returns count of completed lessons per book for the given user across all books. */
    suspend fun getAllBooksHomeworkStatus(
        userId: Long
    ): Result<List<BookHomeworkStatus>>
}
