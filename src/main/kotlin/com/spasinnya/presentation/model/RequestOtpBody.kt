package com.spasinnya.presentation.model

import com.spasinnya.data.repository.database.table.OtpPurpose
import kotlinx.serialization.Serializable

@Serializable
data class RequestOtpBody(
    val email: String,
    val purpose: OtpPurpose = OtpPurpose.LOGIN
)
