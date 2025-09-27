@file:OptIn(ExperimentalTime::class)

package com.spasinnya.data.repository

import com.spasinnya.data.extension.runDb
import com.spasinnya.data.repository.database.table.UserProfiles
import com.spasinnya.data.repository.database.table.Users
import com.spasinnya.domain.model.User
import com.spasinnya.domain.model.UserProfile
import com.spasinnya.domain.repository.UserRepository
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.update
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class UserDataRepository(
    private val database: Database
) : UserRepository {

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

    override suspend fun findUserProfile(userId: Long): Result<UserProfile?> = database.runDb {
        (Users leftJoin UserProfiles)
            .selectAll()
            .where { Users.id eq userId }
            .singleOrNull()
            ?.toUserProfile()
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

