package com.spasinnya.domain.usecase

import com.spasinnya.domain.model.book.Lesson
import com.spasinnya.domain.repository.LessonRepository

class GetLessonsByWeekIdUseCase(
    private val lessonRepository: LessonRepository,
) {
    suspend fun invoke(weekId: Long): Result<List<Lesson>> =
        lessonRepository.getLessonsByWeekId(weekId)
}