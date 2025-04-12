package com.spasinnya.data.repository.database.mapper

import com.spasinnya.data.repository.database.dto.LessonDto
import com.spasinnya.domain.model.book.Lesson

fun LessonDto.toLesson(): Lesson = Lesson(
    number = number,
    title = title,
    quote = quote,
    content = emptyList()
)