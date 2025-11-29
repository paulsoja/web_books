package com.spasinnya.domain.repository

import com.spasinnya.domain.model.book.Lesson

interface LessonRepository {
    suspend fun getLessonsByWeekId(weekId: Long): Result<List<Lesson>>
}