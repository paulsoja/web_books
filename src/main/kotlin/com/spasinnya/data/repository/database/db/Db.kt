package com.spasinnya.data.repository.database.db

import com.spasinnya.data.repository.database.table.OtpTokens
import com.spasinnya.data.repository.database.table.RefreshTokens
import com.spasinnya.data.repository.database.table.UserProfiles
import com.spasinnya.data.repository.database.table.Users
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.net.URI
import javax.sql.DataSource

fun buildHikariFromEnv(): HikariDataSource {
    val dbUrl = System.getenv("DATABASE_URL")

    val cfg = HikariConfig().apply {
        driverClassName = "org.postgresql.Driver"
        maximumPoolSize = (System.getenv("DB_POOL_SIZE") ?: "10").toInt()
        isAutoCommit = false
        transactionIsolation = "TRANSACTION_REPEATABLE_READ"

        if (!dbUrl.isNullOrBlank()) {
            val uri = URI(dbUrl)
            val (username, password) = uri.userInfo.split(":")
            val host = uri.host
            val port = if (uri.port != -1) uri.port else 5432
            val db = uri.path.removePrefix("/")

            jdbcUrl = "jdbc:postgresql://$host:$port/$db?sslmode=require"
            this.username = username
            this.password = password
        } else {
            val host = System.getenv("DB_HOST") ?: "localhost"
            val port = (System.getenv("DB_PORT") ?: "5432").toInt()
            val db = System.getenv("DB_NAME") ?: "books_db"
            val user = System.getenv("DB_USER") ?: "postgres"
            val pass = System.getenv("DB_PASSWORD") ?: "postgres"

            jdbcUrl = "jdbc:postgresql://$host:$port/$db"
            this.username = user
            this.password = pass
        }
    }

    return HikariDataSource(cfg)
}

fun connectAndMigrate(ds: DataSource): Database {
    val db = Database.connect(ds)
    transaction(db) {
        SchemaUtils.createMissingTablesAndColumns(
            Users, UserProfiles, OtpTokens, RefreshTokens
        )
    }
    return db
}