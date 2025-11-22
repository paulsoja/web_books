package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.book.Week
import com.spasinnya.domain.repository.WeekRepository

class GetWeeksUseCase(
    private val weekRepository: WeekRepository,
) {
    suspend fun invoke(bookId: Long): Result<List<Week>> =
        weekRepository.getWeeksByBookId(bookId)
}