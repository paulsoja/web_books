package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object Homeworks : Table("homeworks") {
    val id = long("id").autoIncrement()
    val lessonId = long("lesson_id")
        .references(Lessons.id, onDelete = ReferenceOption.CASCADE)
        .uniqueIndex()                         // UNIQUE (lesson_id)

    val extId = integer("ext_id").nullable()   // исходный id из JSON, если нужен
    val question = text("question").nullable()

    override val primaryKey = PrimaryKey(id)

    init { index("idx_hw_lesson", false, lessonId) }
}