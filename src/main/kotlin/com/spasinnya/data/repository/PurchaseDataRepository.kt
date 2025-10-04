package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.UserPurchases
import com.spasinnya.domain.repository.PurchaseRepository
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll

class PurchaseDataRepository(
    private val database: Database
) : PurchaseRepository {

    override suspend fun addPurchase(
        userId: Long,
        bookId: Long,
        platform: String,
        storeProductId: String,
        purchaseToken: String,
        orderId: String?
    ): Result<Long> = database.runDb {
        UserPurchases.insert {
            it[UserPurchases.userId] = userId
            it[UserPurchases.bookId] = bookId
            it[UserPurchases.platform] = platform
            it[UserPurchases.storeProductId] = storeProductId
            it[UserPurchases.purchaseToken] = purchaseToken
            it[UserPurchases.orderId] = orderId
        } get UserPurchases.id
    }

    override suspend fun isPurchased(userId: Long, bookId: Long): Result<Boolean> = database.runDb {
        UserPurchases
            .selectAll()
            .where { (UserPurchases.userId eq userId) and (UserPurchases.bookId eq bookId) }
            .limit(1)
            .any()
    }

    override suspend fun findBookIdsByUser(userId: Long): Result<List<Long>> = database.runDb {
        UserPurchases
            .selectAll()
            .where { UserPurchases.userId eq userId }
            .map { row -> row[UserPurchases.bookId] }
    }
}