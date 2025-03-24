package com.spasinnya.data.repository.table

import org.jetbrains.exposed.dao.id.IntIdTable

object Books : IntIdTable("books") {
    val number = varchar("number", 10)
    val title = varchar("title", 255)
    val subtitle = varchar("subtitle", 255).nullable()
}