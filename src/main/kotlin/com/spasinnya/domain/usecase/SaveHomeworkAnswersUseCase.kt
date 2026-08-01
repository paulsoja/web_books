package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.homework.HomeworkAnswer
import com.spasinnya.domain.repository.UserAnswerRepository

class SaveHomeworkAnswersUseCase(
    private val userAnswerRepository: UserAnswerRepository
) {
    suspend fun invoke(
        userId: Long,
        bookId: String,
        weekNumber: Int,
        lessonNumber: Int,
        answers: List<HomeworkAnswer>
    ): Result<Unit> =
        userAnswerRepository.upsertLessonAnswers(
            userId = userId,
            bookId = bookId,
            weekNumber = weekNumber,
            lessonNumber = lessonNumber,
            answers = answers
        )
}
