package com.spasinnya.domain.usecase

import com.spasinnya.domain.repository.UserAnswerRepository

class GetLessonsWithAnswersUseCase(
    private val userAnswerRepository: UserAnswerRepository
) {
    suspend operator fun invoke(
        userId: Long,
        bookId: String,
        weekNumber: Int
    ): Result<List<Int>> {
        return userAnswerRepository.getLessonsWithAnswers(
            userId = userId,
            bookId = bookId,
            weekNumber = weekNumber
        )
    }
}
