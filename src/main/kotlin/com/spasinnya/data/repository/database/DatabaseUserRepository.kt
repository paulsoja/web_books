package com.spasinnya.data.repository.database

import com.spasinnya.data.repository.database.table.Users
import com.spasinnya.domain.model.User
import com.spasinnya.domain.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

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

    override suspend fun findByEmail(email: String): User? {
        return dbQuery {
            Users.selectAll()
                .where { Users.email eq email }
                .map { it.toUser() }
                .singleOrNull()
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