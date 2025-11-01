package com.spasinnya

import com.spasinnya.data.repository.ExposedContentSeedRepository
import com.spasinnya.data.service.ExposedTransactionRunner
import com.spasinnya.domain.usecase.SeedBooksFromJsonUseCase
import kotlinx.coroutines.runBlocking

object Bootstrap {

    fun runSeedAllBooksFromClasspath(skipHomework: Boolean = false) {
        val items = ClasspathBooksLoader.loadAllJson()
        if (items.isEmpty()) {
            println("⚠️  No JSON files found in classpath:/books")
            return
        }

        val tx = ExposedTransactionRunner()
        val repo = ExposedContentSeedRepository()
        val useCase = SeedBooksFromJsonUseCase(tx, repo)

        runBlocking {
            items.forEach { (name, json) ->
                println("📘 Seeding from resource: $name")
                useCase.seedFromString(json, skipHomework)
                    .onSuccess { println("✅ Done: $name") }
                    .onFailure { e ->
                        println("❌ Failed on $name: ${e.message}")
                        e.printStackTrace()
                    }
            }
        }
    }
}