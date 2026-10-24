@file:OptIn(ExperimentalTime::class)

package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.*
import com.spasinnya.domain.exception.NotFoundException
import com.spasinnya.domain.model.User
import com.spasinnya.domain.model.UserProfile
import com.spasinnya.domain.repository.UserRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.inSubQuery
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.deleteWhere
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UserDataRepository(
    private val database: Database
) : UserRepository {

    override suspend fun deleteAccount(userId: Long): Result<Unit> = database.runDb {
        val user = Users.selectAll().where { Users.id eq userId }.forUpdate().singleOrNull()
            ?: throw NotFoundException("User not found")
        val email = user[Users.email]

        // Remove indirect references before purchases (their foreign key uses RESTRICT).
        AndroidPurchaseAcknowledgements.deleteWhere {
            purchaseId inSubQuery Purchases.select(Purchases.id).where { Purchases.userId eq userId }
        }
        Purchases.deleteWhere { Purchases.userId eq userId }
        Entitlements.deleteWhere { Entitlements.userId eq userId }
        UserPurchases.deleteWhere { UserPurchases.userId eq userId }
        UserAnswers.deleteWhere { UserAnswers.userId eq userId }
        RefreshTokens.deleteWhere { RefreshTokens.userId eq userId }
        UserProfiles.deleteWhere { UserProfiles.userId eq userId }
        OtpTokens.deleteWhere { OtpTokens.email eq email }
        EmailOtps.deleteWhere { EmailOtps.email eq email }
        Users.deleteWhere { Users.id eq userId }
        Unit
    }

    override suspend fun createUser(email: String, passwordHash: String?, status: String): Result<Long> = database.runDb {
        val now = Clock.System.now()
        Users.insert {
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.status] = status
            it[Users.createdAt] = now
            it[Users.updatedAt] = now
        } get Users.id
    }

    override suspend fun findByEmail(email: String): Result<User?> = database.runDb {
        Users
            .select(Users.columns)
            .where { Users.email eq email }
            .limit(1)
            .firstOrNull()
            ?.toUser()
    }

    override suspend fun findById(id: Long): Result<User?> = database.runDb {
        Users
            .select(Users.columns)
            .where { Users.id eq id }
            .limit(1)
            .firstOrNull()
            ?.toUser()
    }

    override suspend fun setConfirmedAt(userId: Long): Result<Unit> = database.runDb {
        Users.update({ Users.id eq userId }) {
            it[Users.confirmedAt] = Clock.System.now()
            it[Users.status] = "active"
        }
        Unit
    }

    override suspend fun updatePassword(userId: Long, passwordHash: String): Result<Unit> = database.runDb {
        Users.update({ Users.id eq userId }) {
            it[Users.passwordHash] = passwordHash
            it[Users.updatedAt] = Clock.System.now()
        }
        Unit
    }

    override suspend fun updateLastLogin(userId: Long): Result<Unit> = database.runDb {
        Users.update({ Users.id eq userId }) {
            it[Users.lastLoginAt] = Clock.System.now()
        }
        Unit
    }

    override suspend fun createEmptyProfile(userId: Long): Result<Unit> = database.runDb {
        UserProfiles.insert {
            it[UserProfiles.userId] = userId
            it[firstName] = null
            it[lastName] = null
            it[avatarUrl] = null
            it[locale] = null
        }
    }

    override suspend fun findUserProfile(userId: Long): Result<UserProfile?> = database.runDb {
        (Users leftJoin UserProfiles)
            .selectAll()
            .where { Users.id eq userId }
            .singleOrNull()
            ?.toUserProfile()
    }

    override suspend fun updateProfile(
        userId: Long,
        firstName: String?,
        lastName: String?
    ): Result<Unit> = database.runDb {
        UserProfiles
            .update({ UserProfiles.userId eq userId }) { row ->
                if (firstName != null) row[UserProfiles.firstName] = firstName
                if (lastName != null) row[UserProfiles.lastName] = lastName
                row[updatedAt] = CurrentTimestamp
            }
    }

    override suspend fun resetPendingUser(userId: Long, newPasswordHash: String): Result<Unit> = runCatching {
        database.runDb {
            val updated = Users.update({ Users.id eq userId }) { row ->
                row[passwordHash] = newPasswordHash
                row[status] = "pending"
                row[confirmedAt] = null
                row[lastLoginAt] = null
                row[updatedAt] = CurrentTimestamp
            }
            if (updated == 0) throw NotFoundException("User not found")
        }
    }

    private fun ResultRow.toUser() = User(
        id = this[Users.id],
        email = this[Users.email],
        passwordHash = this[Users.passwordHash],
        status = this[Users.status],
        confirmedAt = this[Users.confirmedAt],
        lastLoginAt = this[Users.lastLoginAt],
        role = this[Users.role],
        createdAt = this[Users.createdAt],
        updatedAt = this[Users.updatedAt]
    )

    private fun ResultRow.toUserProfile() = UserProfile(
        userId = this[Users.id],
        email = this[Users.email],
        role = this[Users.role],
        firstName = this[UserProfiles.firstName].orEmpty(),
        lastName = this[UserProfiles.lastName].orEmpty(),
        avatarUrl = this[UserProfiles.avatarUrl].orEmpty()
    )
}

