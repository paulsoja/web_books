package com.spasinnya.presentation.mapper

import com.spasinnya.domain.model.book.Lesson
import com.spasinnya.domain.model.book.LessonContent
import com.spasinnya.presentation.model.LessonContentResponse
import com.spasinnya.presentation.model.LessonResponse

fun Lesson.toPresentation() = LessonResponse(
    id = id,
    weekId = weekId,
    number = number,
    title = title,
    quote = quote,
    content = content.map(LessonContent::toPresentation)
)

fun LessonContent.toPresentation() = LessonContentResponse(
    type = type,
    data = data
)