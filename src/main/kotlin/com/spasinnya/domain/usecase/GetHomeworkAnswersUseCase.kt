package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.homework.HomeworkAnswer
import com.spasinnya.domain.repository.UserAnswerRepository

class GetHomeworkAnswersUseCase(
    private val userAnswerRepository: UserAnswerRepository
) {
    suspend fun invoke(
        userId: Long,
        bookId: String,
        weekNumber: Int,
        lessonNumber: Int
    ): Result<List<HomeworkAnswer>> =
        userAnswerRepository.getLessonAnswers(
            userId = userId,
            bookId = bookId,
            weekNumber = weekNumber,
            lessonNumber = lessonNumber
        )
}
