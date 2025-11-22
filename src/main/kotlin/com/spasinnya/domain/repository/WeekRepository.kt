package com.spasinnya.domain.repository

import com.spasinnya.domain.model.book.Week

interface WeekRepository {
    suspend fun getWeeksByBookId(bookId: Long): Result<List<Week>>
}