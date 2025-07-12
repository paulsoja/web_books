package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.Table


object Users : Table("users") {
    val id = integer("id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val password = varchar("password", 255)
    val otpCode = varchar("otp_code", 6).nullable()
    val isConfirmed = bool("is_confirmed").default(false)
    override val primaryKey = PrimaryKey(id)
}