package com.spasinnya.presentation.model

import kotlinx.serialization.Serializable

@Serializable
data class IosPurchaseRequest(val signedTransaction: String)

@Serializable
data class StorePurchaseResponse(val productId: Long, val granted: Boolean)

@Serializable
data class EntitlementsResponse(val products: List<Long>)
