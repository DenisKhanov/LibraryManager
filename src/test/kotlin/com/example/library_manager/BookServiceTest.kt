package com.example.library_manager


import com.example.library_manager.controller.dto.book.BookCreateRequest
import com.example.library_manager.controller.dto.book.MessageResponse
import com.example.library_manager.controller.maper.toDomain
import com.example.library_manager.domain.Book
import com.example.library_manager.repository.impl.BookRepository
import com.example.library_manager.repository.jpa.entity.book.BookEntity

import com.example.library_manager.service.BookService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


@ExtendWith(MockitoExtension::class)
class BookServiceTest {

    @Mock
    private lateinit var bookRepository: BookRepository

    @InjectMocks
    private lateinit var bookService: BookService

    @Test
    fun `should create book successfully`() {
        val request = BookCreateRequest(
            title = "1984",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1949
        )
        val book = Book(
            title = request.title,
            author = request.author,
            isbn = request.isbn,
            publishedYear = request.publishedYear
        )
        val savedBook = book.copy(id = 1)
        whenever(bookRepository.existsByIsbn(request.isbn)).thenReturn(false)
        whenever(bookRepository.save(book)).thenReturn(savedBook)

        val response = bookService.createBook(request.toDomain())

        assertEquals(1, response.id)
        assertEquals("1984", response.title)
        verify(bookRepository).existsByIsbn(request.isbn)
        verify(bookRepository).save(book)
    }

    @Test
    fun `should throw exception for duplicate ISBN`() {
        val request = BookCreateRequest(
            title = "1984",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1949
        )
        whenever(bookRepository.existsByIsbn(request.isbn)).thenReturn(true)

        val exception = assertFailsWith<ResponseStatusException> {
            bookService.createBook(request.toDomain())
        }
        assertEquals(HttpStatus.BAD_REQUEST, exception.statusCode)
        assertEquals("Book with ISBN ${request.isbn} already exists", exception.reason)
    }

    @Test
    fun `should delete book successfully`() {
        val bookId = 1L
        whenever(bookRepository.existsById(bookId)).thenReturn(true)

        val response = bookService.deleteBook(bookId)

        assertEquals(MessageResponse("Book with id $bookId deleted successfully"), response)
        verify(bookRepository).existsById(bookId)
        verify(bookRepository).deleteById(bookId)
    }

    @Test
    fun `should throw exception for deleting non-existing book`() {
        val bookId = 999L
        whenever(bookRepository.existsById(bookId)).thenReturn(false)

        val exception = assertFailsWith<ResponseStatusException> {
            bookService.deleteBook(bookId)
        }
        assertEquals(HttpStatus.NOT_FOUND, exception.statusCode)
        assertEquals("Book with id $bookId not found", exception.reason)
    }
}