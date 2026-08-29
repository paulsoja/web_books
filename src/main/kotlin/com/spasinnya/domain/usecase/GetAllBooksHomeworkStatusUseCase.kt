package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.homework.BookHomeworkStatus
import com.spasinnya.domain.repository.UserAnswerRepository

class GetAllBooksHomeworkStatusUseCase(
    private val userAnswerRepository: UserAnswerRepository
) {
    suspend operator fun invoke(
        userId: Long
    ): Result<List<BookHomeworkStatus>> {
        return userAnswerRepository.getAllBooksHomeworkStatus(
            userId = userId
        )
    }
}
