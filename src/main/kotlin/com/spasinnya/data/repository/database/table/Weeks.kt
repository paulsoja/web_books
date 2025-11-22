package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object Weeks : Table("weeks") {
    val id = long("id").autoIncrement()
    val bookId = long("book_id").references(Books.id, onDelete = ReferenceOption.CASCADE)
    val number = integer("number")
    val title = text("title")

    init { uniqueIndex(bookId, number) }
    override val primaryKey = PrimaryKey(id)
}