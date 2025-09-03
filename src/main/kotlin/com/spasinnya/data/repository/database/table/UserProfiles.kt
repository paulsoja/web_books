package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.timestamp
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
object UserProfiles : Table("user_profiles") {
    val userId = long("user_id").uniqueIndex().references(Users.id, onDelete = ReferenceOption.CASCADE)
    val firstName = text("first_name").nullable()
    val lastName = text("last_name").nullable()
    val avatarUrl = text("avatar_url").nullable()
    val locale = text("locale").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    override val primaryKey = PrimaryKey(userId)
}