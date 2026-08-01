package com.spasinnya.data.repository.database.table

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ColumnType
import org.jetbrains.exposed.v1.core.Table
import org.postgresql.util.PGobject

/**
 * Minimal PostgreSQL `jsonb` column type.
 *
 * The project does not depend on the `exposed-json` module, so this maps a
 * `jsonb` column to/from a raw JSON [String] via the PostgreSQL driver's
 * [PGobject]. Serialize/deserialize the string with kotlinx.serialization at
 * the repository layer.
 */
class JsonbColumnType : ColumnType<String>() {
    override fun sqlType(): String = "jsonb"

    override fun valueFromDB(value: Any): String = when (value) {
        is PGobject -> value.value.orEmpty()
        is String -> value
        else -> value.toString()
    }

    override fun notNullValueToDB(value: String): Any = PGobject().apply {
        type = "jsonb"
        this.value = value
    }

    override fun nonNullValueToString(value: String): String =
        "'${value.replace("'", "''")}'"
}

/** Registers a `jsonb` column storing raw JSON as a [String]. */
fun Table.jsonb(name: String): Column<String> = registerColumn(name, JsonbColumnType())
