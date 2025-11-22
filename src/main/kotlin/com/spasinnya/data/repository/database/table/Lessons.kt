package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object Lessons : Table("lessons") {
    val id = long("id").autoIncrement()
    val weekId = long("week_id").references(Weeks.id, onDelete = ReferenceOption.CASCADE)
    val number = integer("number")
    val title = text("title")
    val quote = text("quote").nullable()

    init { uniqueIndex(weekId, number) } // внутри недели номер урока уникален
    override val primaryKey = PrimaryKey(id)
}