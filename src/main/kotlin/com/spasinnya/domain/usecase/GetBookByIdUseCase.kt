package com.spasinnya.domain.usecase

import com.spasinnya.domain.repository.BookRepository

class GetBookByIdUseCase(private val bookRepository: BookRepository) {
    fun execute(bookId: Int) = bookRepository.getBookById(bookId)
}