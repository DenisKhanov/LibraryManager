package com.example.library_manager.repository.maper

import com.example.library_manager.domain.Book
import com.example.library_manager.repository.jpa.entity.book.BookEntity

fun BookEntity.toDomain() = Book(
    id = id,
    isbn = isbn,
    title = title,
    author = author,
    publishedYear = publishedYear,
    libraryItems = libraryItems,
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