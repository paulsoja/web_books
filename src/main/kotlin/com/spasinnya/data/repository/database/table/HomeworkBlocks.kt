package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table

object HomeworkBlocks : Table("homework_blocks") {
    val id = long("id").autoIncrement()
    val homeworkId = long("homework_id")
        .references(Homeworks.id, onDelete = ReferenceOption.CASCADE)

    // позиция элемента в блоке (уникальна в рамках одного ДЗ)
    val order = integer("order_index")

    // Postgres enum homework_component
    val component = enumerationByName<HomeworkComponent>("component", length = 32)

    // универсальные поля под разные компоненты
    val answer = text("answer_label").nullable()  // для radio/check вариантов
    val extra = text("extra_text").nullable()     // для edit_text/text/text_skip

    init {
        uniqueIndex(homeworkId, order)            // UNIQUE (homework_id, order_index)
        index("idx_hw_blocks_hw", false, homeworkId, order)
    }

    override val primaryKey = PrimaryKey(id)
}