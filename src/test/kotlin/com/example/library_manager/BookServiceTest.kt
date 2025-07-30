package com.example.library_manager

import com.example.library_manager.controller.dto.book.BookCreateRequest
import com.example.library_manager.controller.mapper.toDomain
import com.example.library_manager.domain.Book
import com.example.library_manager.repository.impl.BookRepository
import com.example.library_manager.service.BookService
import com.example.library_manager.service.exceptions.DuplicateIsbnCustomException
import com.example.library_manager.service.exceptions.ResourceNotFoundCustomException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
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

        val exception = assertFailsWith<DuplicateIsbnCustomException> {
            bookService.createBook(request.toDomain())
        }

        assertEquals("Book with ISBN ${request.isbn} already exists", exception.message)
    }

    @Test
    fun `should delete book successfully`() {
        val bookId = 1L
        whenever(bookRepository.existsById(bookId)).thenReturn(true)
        bookService.deleteBook(bookId)
        verify(bookRepository).existsById(bookId)
        verify(bookRepository).deleteById(bookId)
    }

    @Test
    fun `should throw exception for deleting non-existing book`() {
        val bookId = 999L
        whenever(bookRepository.existsById(bookId)).thenReturn(false)

        val exception = assertFailsWith<ResourceNotFoundCustomException> {
            bookService.deleteBook(bookId)
        }
        assertEquals("Book with ID $bookId not found", exception.message)
    }
}