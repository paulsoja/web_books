@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.AndroidPurchaseAcknowledgements as Ack
import com.spasinnya.data.repository.database.table.Purchases
import com.spasinnya.domain.model.purchase.StorePlatform
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.lessEq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes

data class PendingAndroidAcknowledgement(val purchaseId: Long, val productId: String, val token: String)

class AndroidAcknowledgementRepository(private val database: Database) {
    suspend fun pending(): List<PendingAndroidAcknowledgement> = database.runDb {
        (Ack innerJoin Purchases).selectAll().where {
            (Ack.acknowledged eq false) and (Ack.nextAttemptAt lessEq Clock.System.now())
        }.orderBy(Ack.nextAttemptAt).limit(100).map {
            PendingAndroidAcknowledgement(it[Ack.purchaseId], it[Purchases.storeProductId], it[Ack.purchaseToken])
        }
    }.getOrThrow()

    suspend fun complete(transactionId: String) = database.runDb {
        val id = Purchases.selectAll().where {
            (Purchases.platform eq StorePlatform.ANDROID) and
                (Purchases.storeTransactionId eq transactionId)
        }.single()[Purchases.id]
        Ack.update({ Ack.purchaseId eq id }) { it[acknowledged] = true }
    }.getOrThrow()

    suspend fun retryLater(id: Long) = database.runDb {
        Ack.update({ Ack.purchaseId eq id }) { it[nextAttemptAt] = Clock.System.now() + 5.minutes }
    }.getOrThrow()
}
