package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object UserPurchases : Table("user_purchases") {
    val id = long("id").autoIncrement()

    val userId = long("user_id")
        .references(Users.id, onDelete = ReferenceOption.CASCADE)

    // Books — IntIdTable ⇒ используем reference("col", Table)
    val bookId = long("book_id").references(Books.id, onDelete = ReferenceOption.RESTRICT)

    val platform = varchar("platform", 10) // "google" | "apple"
    val storeProductId = text("store_product_id")
    val purchaseToken = text("purchase_token").uniqueIndex()
    val orderId = text("order_id").nullable().uniqueIndex()

    val acknowledged = bool("acknowledged").default(false)
    val purchasedAt = timestamp("purchased_at").defaultExpression(CurrentTimestamp)
    val revokedAt = timestamp("revoked_at").nullable()

    override val primaryKey = PrimaryKey(id)

    init {
        uniqueIndex(userId, bookId) // один раз можно купить книгу
    }
}