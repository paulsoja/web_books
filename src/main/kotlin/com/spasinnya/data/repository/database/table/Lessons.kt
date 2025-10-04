package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.Table


object Lessons : Table("lessons") {
    val id = long("id").autoIncrement()
    val number = integer("number")
    val title = varchar("title", 255)
    val quote = text("quote").nullable()
}