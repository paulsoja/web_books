package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object LessonBlocks : Table("lesson_content_blocks") {
    val id = long("id").autoIncrement()
    val lessonId = long("lesson_id").references(Lessons.id, onDelete = ReferenceOption.CASCADE)
    val order = integer("order_index")
    val blockType = enumerationByName<ContentBlockType>("block_type", length = 32)
    val dataText = text("data_text").nullable()
    val dataImage = text("data_image").nullable()

    init {
        uniqueIndex(lessonId, order)           // UNIQUE (lesson_id, order_index)
        index("idx_blocks_lesson", false, lessonId, order)
    }

    override val primaryKey = PrimaryKey(id)
}