package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.dao.id.IntIdTable

object Lessons : IntIdTable("lessons") {
    val weekId = reference("week_id", Weeks)
    val number = integer("number")
    val title = varchar("title", 255)
    val quote = text("quote").nullable()
}