package com.spasinnya.domain.repository

interface PurchaseRepository {
    suspend fun addPurchase(
        userId: Long,
        bookId: Long,
        platform: String,
        storeProductId: String,
        purchaseToken: String,
        orderId: String?
    ): Result<Long>

    suspend fun isPurchased(userId: Long, bookId: Long): Result<Boolean>

    suspend fun findBookIdsByUser(userId: Long): Result<List<Long>>

    suspend fun markPurchased(userId: Long, bookId: Long): Result<Unit>
}