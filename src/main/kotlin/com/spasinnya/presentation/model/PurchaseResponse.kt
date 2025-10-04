package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class PurchaseResponse(
    val bookId: Long,
    val purchased: Boolean
)
