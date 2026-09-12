@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.spasinnya.domain.model.purchase

import kotlin.time.Instant

enum class StorePlatform { IOS, ANDROID }
enum class StoreEnvironment { SANDBOX, PRODUCTION }

data class PurchaseProof(val platform: StorePlatform, val value: String)

data class VerifiedPurchase(
    val platform: StorePlatform,
    val storeProductId: String,
    val storeTransactionId: String,
    val purchasedAt: Instant,
    val environment: StoreEnvironment
)

data class Purchase(
    val id: Long,
    val userId: Long,
    val platform: StorePlatform,
    val productId: Long,
    val storeTransactionId: String,
    val purchasedAt: Instant,
    val createdAt: Instant
)

data class Entitlement(val id: Long, val userId: Long, val productId: Long, val grantedAt: Instant)
