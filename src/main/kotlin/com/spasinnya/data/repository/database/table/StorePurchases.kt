@file:OptIn(kotlin.time.ExperimentalTime::class)

package com.spasinnya.data.repository.database.table

import com.spasinnya.domain.model.purchase.StorePlatform
import com.spasinnya.domain.model.purchase.StoreEnvironment
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp

object StoreProductMappings : Table("store_product_mappings") {
    val platform = enumerationByName<StorePlatform>("platform", 10)
    val storeProductId = varchar("store_product_id", 255)
    val productId = long("product_id").references(Books.id, onDelete = ReferenceOption.RESTRICT)
    override val primaryKey = PrimaryKey(platform, storeProductId)
}

object Purchases : Table("purchases") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.RESTRICT)
    val platform = enumerationByName<StorePlatform>("platform", 10)
    val productId = long("product_id").references(Books.id, onDelete = ReferenceOption.RESTRICT)
    val storeProductId = varchar("store_product_id", 255)
    val storeTransactionId = varchar("store_transaction_id", 255)
    val environment = enumerationByName<StoreEnvironment>("environment", 10)
    val purchasedAt = timestamp("purchased_at")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    override val primaryKey = PrimaryKey(id)
    init { uniqueIndex(platform, storeTransactionId) }
}

object Entitlements : Table("entitlements") {
    val id = long("id").autoIncrement()
    val userId = long("user_id").references(Users.id, onDelete = ReferenceOption.CASCADE)
    val productId = long("product_id").references(Books.id, onDelete = ReferenceOption.RESTRICT)
    val grantedAt = timestamp("granted_at").defaultExpression(CurrentTimestamp)
    override val primaryKey = PrimaryKey(id)
    init { uniqueIndex(userId, productId) }
}
