@file:OptIn(ExperimentalTime::class)

package com.spasinnya.presentation.mapper

import com.spasinnya.domain.model.User
import com.spasinnya.presentation.model.UserResponse
import kotlin.time.ExperimentalTime

fun User.toResponse(): UserResponse = UserResponse(
    id,
    email,
    status,
)