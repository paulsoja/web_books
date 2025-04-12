package com.spasinnya.data.repository.database.mapper

import com.spasinnya.data.repository.database.dto.WeekDto
import com.spasinnya.domain.model.book.Week

fun WeekDto.toWeek(): Week = Week(
    number = number,
    title = title,
    lessons = lessons.map { it.toLesson() }
)