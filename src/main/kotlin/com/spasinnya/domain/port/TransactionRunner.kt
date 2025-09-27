package com.spasinnya.domain.port

interface TransactionRunner {
    suspend fun <T> inTransaction(block: suspend () -> T): T
}