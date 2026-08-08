package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.homework.WeekHomeworkStatus
import com.spasinnya.domain.repository.UserAnswerRepository

class GetBookHomeworkStatusUseCase(
    private val userAnswerRepository: UserAnswerRepository
) {
    suspend operator fun invoke(
        userId: Long,
        bookId: String
    ): Result<List<WeekHomeworkStatus>> {
        return userAnswerRepository.getHomeworkStatusByBookId(
            userId = userId,
            bookId = bookId
        )
    }
}
