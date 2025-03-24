package com.spasinnya.domain.usecase

import com.spasinnya.domain.repository.BookRepository

class GetBooksUseCase(private val bookRepository: BookRepository) {
    fun execute() = bookRepository.getAllBooks()
}