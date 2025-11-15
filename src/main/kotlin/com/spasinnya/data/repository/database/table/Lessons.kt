package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime


object Lessons : Table("lessons") {
    val id = long("id").autoIncrement()
    val weekId = long("week_id").references(Weeks.id, onDelete = ReferenceOption.CASCADE)
    val number = integer("number")
    val title = text("title")
    val quote = text("quote").nullable()
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)

    init { uniqueIndex(weekId, number) } // внутри недели номер урока уникален
    override val primaryKey = PrimaryKey(id)
}