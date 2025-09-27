package com.spasinnya.data.service

import com.spasinnya.domain.port.TransactionRunner
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.v1.jdbc.transactions.experimental.newSuspendedTransaction

class ExposedTransactionRunner : TransactionRunner {
    override suspend fun <T> inTransaction(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}