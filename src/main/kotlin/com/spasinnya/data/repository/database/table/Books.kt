package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.Table


object Books : Table("books") {
    val id = long("id").autoIncrement()
    val number = varchar("number", 10)
    val title = varchar("title", 255)
    val subtitle = varchar("subtitle", 255).nullable()
    override val primaryKey = PrimaryKey(id)
}