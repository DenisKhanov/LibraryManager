package com.example.library_manager.service

import com.example.library_manager.domain.book.Book
import com.example.library_manager.dto.book.BookRequest
import com.example.library_manager.dto.book.BookResponse
import com.example.library_manager.dto.book.MessageResponse
import com.example.library_manager.repository.BookRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class BookService(private val bookRepository: BookRepository) {
    @Transactional
    fun createBook(request: BookRequest): BookResponse {
        if (bookRepository.existsByIsbn(request.isbn)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Book with ISBN ${request.isbn} already exists")
        }
        val book = Book(
            title = request.title,
            author = request.author,
            isbn = request.isbn,
            publishedYear = request.publishedYear
        )
        val savedBook = bookRepository.save(book)
        return BookResponse(
            id = savedBook.id!!,
            title = savedBook.title,
            author = savedBook.author,
            isbn = savedBook.isbn,
            publishedYear = savedBook.publishedYear
        )
    }

    fun getAllBooks(pageable: Pageable): Page<BookResponse> {
        return bookRepository.findAll(pageable).map { book ->
            BookResponse(
                id = book.id!!,
                title = book.title,
                author = book.author,
                isbn = book.isbn,
                publishedYear = book.publishedYear
            )
        }
    }

    @Transactional
    fun updateBook(id: Long, request: BookRequest) {
        val book = bookRepository.findById(id)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Book with id $id not found") }

        if (request.isbn != book.isbn && bookRepository.existsByIsbn(request.isbn)) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Book with ISBN ${request.isbn} already exists")
        }

        bookRepository.save(
            book.copy(
                title = request.title,
                author = request.author,
                isbn = request.isbn,
                publishedYear = request.publishedYear
            )
        )
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