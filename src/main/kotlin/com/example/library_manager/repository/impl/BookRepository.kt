package com.example.library_manager.repository.impl

import com.example.library_manager.domain.Book
import com.example.library_manager.repository.jpa.BookJpaRepository
import com.example.library_manager.repository.mapper.toDomain
import com.example.library_manager.repository.mapper.toEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class BookRepository (private val bookJpaRepository: BookJpaRepository){
    fun findById(id: Long): Book? {
        val book = bookJpaRepository.findByIdOrNull(id)
        return book?.toDomain()
    }

    fun existsByIsbn(isbn: String): Boolean {
        return bookJpaRepository.existsByIsbn(isbn)
    }

    fun existsById(id: Long): Boolean {
        return bookJpaRepository.existsById(id)
    }

    fun save(book: Book): Book {
        val entity = book.toEntity()
        val savedEntity = bookJpaRepository.save(entity)
        return savedEntity.toDomain()
    }

    fun findAll(pageable: Pageable): Page<Book> {
        return bookJpaRepository.findAll(pageable).map { it.toDomain() }
    }

    fun deleteById(id: Long) {
        bookJpaRepository.deleteById(id)
    }
}