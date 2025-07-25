package com.example.library_manager.service

import com.example.library_manager.controller.dto.book.MessageResponse
import com.example.library_manager.domain.Book
import com.example.library_manager.repository.impl.BookRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class BookService(private val bookRepository: BookRepository) {
    @Transactional
    fun createBook(book: Book): Book {
        if (bookRepository.existsByIsbn(book.isbn)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Book with ISBN ${book.isbn} already exists")
        }
        val savedBook = bookRepository.save(book)
        return savedBook
    }

    fun getAllBooks(pageable: Pageable): Page<Book> {
        return bookRepository.findAll(pageable)
    }


    @Transactional
    fun updateBook(book: Book) {

        val oldBook = bookRepository.findById(book.id!!) ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book with id ${book.id} not found")

        if (oldBook.isbn != book.isbn && bookRepository.existsByIsbn(book.isbn)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Book with ISBN ${book.isbn} already exists")
        }

        bookRepository.save(book)
        return
    }

    @Transactional
    fun deleteBook(id: Long): MessageResponse {
        if (!bookRepository.existsById(id)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Book with id $id not found")
        }
        bookRepository.deleteById(id)
        return MessageResponse("Book with id $id deleted successfully")
    }
}