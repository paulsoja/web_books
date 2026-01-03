package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime


object Books : Table("books") {
    val id = long("id")
    val number = text("number")
    val title = text("title")
    val subtitle = text("subtitle").nullable()
    val language = varchar("language", length = 12).default("en")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    override val primaryKey = PrimaryKey(id)
}