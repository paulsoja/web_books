package com.spasinnya.data.extension

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

suspend fun <T> Database.runDb(block: suspend () -> T): Result<T> =
    runCatching { newSuspendedTransaction(db = this@runDb) { block() } }