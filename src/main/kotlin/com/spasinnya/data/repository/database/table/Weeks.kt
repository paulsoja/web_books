package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object Weeks : Table("weeks") {
    val id = long("id").autoIncrement()
    val bookId = long("book_id").references(Books.id, onDelete = ReferenceOption.CASCADE)
    val number = integer("week_number")
    val title = text("title")
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    init { uniqueIndex(bookId, number) }
    override val primaryKey = PrimaryKey(id)
}