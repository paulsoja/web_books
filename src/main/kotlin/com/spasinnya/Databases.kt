package com.spasinnya

import com.spasinnya.data.repository.table.Users
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val url = "jdbc:postgresql://localhost:5432/mentoring_db"
        val driver = "org.postgresql.Driver"
        val user = "postgres"
        val password = "root"

        Database.connect(
            url = url,
            driver = driver,
            user = user,
            password = password
        )

        transaction {
            SchemaUtils.create(Users)
            println("Table 'Users' has been created.")
        }
    }
}
