package com.example.library_manager

import com.example.library_manager.controller.dto.library_item.LibraryItemRequest
import com.example.library_manager.domain.Book
import com.example.library_manager.domain.LibraryItem
import com.example.library_manager.repository.impl.BookRepository
import com.example.library_manager.repository.impl.LibraryItemRepository
import com.example.library_manager.service.LibraryItemService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals


@ExtendWith(MockitoExtension::class)
class LibraryItemServiceTest {

    @Mock
    private lateinit var libraryItemRepository: LibraryItemRepository

    @Mock
    private lateinit var bookRepository: BookRepository

    @InjectMocks
    private lateinit var libraryItemService: LibraryItemService

    @Test
    fun `should create library item successfully`() {

        val itemRequest = LibraryItemRequest(
            bookId = 1,
            totalCopies = 2,
        )

        val foundedBook = Book(
            id = itemRequest.bookId,
            title = "book title",
            author = "book author",
            isbn = "9780451524935",
            publishedYear = 1990,
        )

        val libraryItem = LibraryItem(
            book = foundedBook,
            totalCopies = itemRequest.totalCopies,
            availableCopies = itemRequest.totalCopies,
        )
        val savedLibraryItem = libraryItem.copy(id = 1)
        whenever(bookRepository.findById(itemRequest.bookId)).thenReturn(foundedBook)
        whenever(libraryItemRepository.save(libraryItem)).thenReturn(savedLibraryItem)

        val response = libraryItemService.createLibraryItem(itemRequest.bookId, itemRequest.totalCopies)

        assertEquals(1, response.id)
        assertEquals(foundedBook, response.book)
        assertEquals(2, response.totalCopies)
        assertEquals(2, response.availableCopies)
        verify(bookRepository).findById(itemRequest.bookId)
        verify(libraryItemRepository).save(libraryItem)
    }
}