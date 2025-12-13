package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class RequestOtpBody(val email: String)
