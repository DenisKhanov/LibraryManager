package com.example.library_manager.service

import com.example.library_manager.domain.Book
import com.example.library_manager.repository.impl.BookRepository
import com.example.library_manager.service.exceptions.DuplicateIsbnCustomException
import com.example.library_manager.service.exceptions.ResourceNotFoundCustomException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class BookService(private val bookRepository: BookRepository) {
    @Transactional
    fun createBook(book: Book): Book {
        if (bookRepository.existsByIsbn(book.isbn)) {
            throw DuplicateIsbnCustomException(book.isbn)
        }
        val savedBook = bookRepository.save(book)
        return savedBook
    }

    fun getAllBooks(pageable: Pageable): Page<Book> {
        return bookRepository.findAll(pageable)
    }


    @Transactional
    fun updateBook(book: Book) {

        val oldBook = bookRepository.findById(book.id!!) ?: throw ResourceNotFoundCustomException("Book", book.id)

        if (oldBook.isbn != book.isbn && bookRepository.existsByIsbn(book.isbn)) {
            throw DuplicateIsbnCustomException(book.isbn)
        }

        bookRepository.save(book)
    }

    @Transactional
    fun deleteBook(id: Long) {
        if (!bookRepository.existsById(id)) throw ResourceNotFoundCustomException("Book", id)
        bookRepository.deleteById(id)
    }
}