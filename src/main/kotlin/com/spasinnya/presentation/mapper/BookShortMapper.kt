package com.spasinnya.presentation.mapper

import com.spasinnya.domain.model.book.BookShort
import com.spasinnya.presentation.model.BookShortResponse

fun BookShort.toPresentation(): BookShortResponse = BookShortResponse(
    id = id,
    number = number,
    title = title,
    subtitle = subtitle,
    isPurchased = isPurchased
)