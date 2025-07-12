package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable


object Books : IntIdTable("books") {
    val number = varchar("number", 10)
    val title = varchar("title", 255)
    val subtitle = varchar("subtitle", 255).nullable()
}