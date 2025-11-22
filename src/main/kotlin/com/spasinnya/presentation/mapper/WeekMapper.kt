package com.spasinnya.presentation.mapper

import com.spasinnya.domain.model.book.Week
import com.spasinnya.presentation.model.WeekResponse

fun Week.toPresentation() = WeekResponse(
    id = id,
    bookId = bookId,
    number = number,
    title = title,
)