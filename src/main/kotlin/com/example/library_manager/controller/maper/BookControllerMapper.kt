package com.example.library_manager.controller.maper

import com.example.library_manager.controller.dto.book.BookCreateRequest
import com.example.library_manager.controller.dto.book.BookResponse
import com.example.library_manager.controller.dto.book.BookUpdateRequest
import com.example.library_manager.domain.Book
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

fun Book.toResponse(): BookResponse {
    val bookId = id ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Book ID is null") as Throwable
    return BookResponse(
        id = bookId,
        title = title,
        author = author,
        isbn = isbn,
        publishedYear = publishedYear,
    )
}

fun BookCreateRequest.toDomain(): Book {
    return Book(
        title = title,
        author = author,
        isbn = isbn,
        publishedYear = publishedYear,
    )
}

fun BookUpdateRequest.toDomain(id: Long): Book {
    return Book(
        id = id,
        title = title,
        author = author,
        isbn = isbn,
        publishedYear = publishedYear,
    )
}

