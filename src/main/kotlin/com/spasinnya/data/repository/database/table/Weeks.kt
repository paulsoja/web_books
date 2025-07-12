package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable


object Weeks : IntIdTable("weeks") {
    val bookId = reference("book_id", Books)
    val number = integer("number")
    val title = varchar("title", 255)
}