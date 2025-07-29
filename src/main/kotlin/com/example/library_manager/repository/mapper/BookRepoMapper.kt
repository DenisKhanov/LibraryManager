package com.example.library_manager.repository.mapper

import com.example.library_manager.domain.Book
import com.example.library_manager.repository.jpa.entity.book.BookEntity

fun BookEntity.toDomain() = Book(
    id = id!!, //TODO{добавить проверку}
    isbn = isbn,
    title = title,
    author = author,
    publishedYear = publishedYear,
)

fun Book.toEntity(): BookEntity {
    return BookEntity(
        id = id,
        title = title,
        author = author,
        isbn = isbn,
        publishedYear = publishedYear,
    )
}