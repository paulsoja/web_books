package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.Entitlements
import com.spasinnya.domain.repository.EntitlementRepository
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.selectAll

class EntitlementDataRepository(private val database: Database) : EntitlementRepository {
    override suspend fun findProductIdsByUser(userId: Long): Result<List<Long>> = database.runDb {
        Entitlements.selectAll().where { Entitlements.userId eq userId }
            .orderBy(Entitlements.productId).map { it[Entitlements.productId] }
    }
}
