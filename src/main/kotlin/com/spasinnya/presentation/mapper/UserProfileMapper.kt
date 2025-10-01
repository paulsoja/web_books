package com.spasinnya.presentation.mapper

import com.spasinnya.domain.model.UserProfile
import com.spasinnya.presentation.model.UserProfileResponse

fun UserProfile.toResponse(): UserProfileResponse = UserProfileResponse(
    userId = userId,
    email = email,
    role = role,
    firstName = firstName,
    lastName = lastName,
    avatarUrl = avatarUrl
)