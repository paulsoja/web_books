package com.spasinnya.data.repository.database.db

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.v1.jdbc.Database
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

fun connectFlyway(ds: DataSource) {
    Flyway.configure()
        .dataSource(ds)
        .baselineVersion("0")
        .baselineOnMigrate(true)
        .load()
        .migrate()
}

fun connectAndMigrate(ds: DataSource): Database {
    val db = Database.connect(ds)
    return db
}