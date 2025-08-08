package com.spasinnya.data.repository.database

import com.spasinnya.data.repository.database.table.Users
import com.spasinnya.domain.exception.UserNotFoundException
import com.spasinnya.domain.model.User
import com.spasinnya.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.core.ResultRow
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.jetbrains.exposed.v1.jdbc.update

class DatabaseUserRepository : UserRepository {

    override suspend fun createUser(email: String, password: String, otpCode: String): User {
        return dbQuery {
            try {
                val insertedRow = Users.insert {
                    it[Users.email] = email
                    it[Users.password] = password
                    it[Users.otpCode] = otpCode
                }[Users.id]

                User(
                    id = insertedRow,
                    email = email,
                    password = password,
                    otpCode = otpCode,
                    isConfirmed = false
                )
            } catch (e: Exception) {
                User(
                    id = -1,
                    email = email,
                    password = password,
                    otpCode = otpCode,
                    isConfirmed = false
                )
            }
        }
    }

    override suspend fun findByEmail(email: String): Result<User> {
        return dbQuery {
            Users.selectAll()
                .where { Users.email eq email }
                .map(ResultRow::toUser)
                .singleOrNull()
                ?.let { Result.success(it) }
                ?: Result.failure(UserNotFoundException())
        }
    }

    override suspend fun confirmUser(id: Int) {
        transaction {
            Users.update({ Users.id eq id }) {
                it[isConfirmed] = true
            }
        }
    }

    override suspend fun updatePassword(email: String, newPassword: String) {
        transaction {
            Users.update({ Users.email eq email }) {
                it[password] = newPassword
            }
        }
    }
}

suspend fun <T> dbQuery(block: suspend () -> T): T =
    newSuspendedTransaction(Dispatchers.IO) { block() }

private fun ResultRow.toUser() = User(
    id = this[Users.id],
    email = this[Users.email],
    password = this[Users.password],
    otpCode = this[Users.otpCode].orEmpty(),
    isConfirmed = this[Users.isConfirmed]
)