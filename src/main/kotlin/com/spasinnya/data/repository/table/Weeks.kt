package com.spasinnya.data.repository.table

import org.jetbrains.exposed.dao.id.IntIdTable

object Weeks : IntIdTable("weeks") {
    val bookId = reference("book_id", Books)
    val number = integer("number")
    val title = varchar("title", 255)
}