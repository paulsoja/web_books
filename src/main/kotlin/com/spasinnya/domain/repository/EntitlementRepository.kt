package com.spasinnya.domain.repository

interface EntitlementRepository {
    suspend fun findProductIdsByUser(userId: Long): Result<List<Long>>
}
