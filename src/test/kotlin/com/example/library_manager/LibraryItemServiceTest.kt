package com.example.library_manager

import com.example.library_manager.controller.dto.library_item.LibraryItemRequest
import com.example.library_manager.domain.Book
import com.example.library_manager.domain.LibraryItem
import com.example.library_manager.repository.impl.BookRepository
import com.example.library_manager.repository.impl.LibraryItemRepository
import com.example.library_manager.service.LibraryItemService
import com.example.library_manager.service.exceptions.IllegalArgumentCustomException
import com.example.library_manager.service.exceptions.ResourceNotFoundCustomException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith


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

    @Test
    fun `should throw ResourceNotFoundCustomException when book not found`() {
        whenever(bookRepository.findById(999L)).thenReturn(null)

        assertFailsWith<ResourceNotFoundCustomException> {
            libraryItemService.createLibraryItem(999L, 2)
        }.also {
            assertEquals("Book with ID 999 not found", it.message)
        }
        verify(bookRepository).findById(999L)
        verify(libraryItemRepository, never()).save(org.mockito.kotlin.any())
    }


    @Test
    fun `should throw IllegalArgumentCustomException when totalCopies is invalid`() {
        val book = Book(
            id = 1L,
            title = "book title",
            author = "book author",
            isbn = "9780451524935",
            publishedYear = 1990
        )
        whenever(bookRepository.findById(1L)).thenReturn(book)

        assertFailsWith<IllegalArgumentCustomException> {
            libraryItemService.createLibraryItem(1L, 0)
        }.also {
            assertEquals("Total copies must be greater than 0", it.message)
        }
        verify(bookRepository).findById(1L)
        verify(libraryItemRepository,never()).save(org.mockito.kotlin.any())
    }

    // Тест: получение списка LibraryItem с пагинацией
    @Test
    fun `should return paginated list of library items`() {
        val pageable = PageRequest.of(0, 10)
        val book = Book(
            id = 1L,
            title = "book title",
            author = "book author",
            isbn = "9780451524935",
            publishedYear = 1990
        )
        val libraryItem = LibraryItem(
            id = 1L,
            book = book,
            totalCopies = 2,
            availableCopies = 2
        )
        val page = PageImpl(listOf(libraryItem), pageable, 1)
        whenever(libraryItemRepository.findAll(pageable)).thenReturn(page)

        val response = libraryItemService.getAllLibraryItems(pageable)

        assertEquals(1, response.content.size)
        assertEquals(libraryItem, response.content[0])
        assertEquals(0, response.pageable.pageNumber)
        assertEquals(10, response.pageable.pageSize)
        assertEquals(1, response.totalElements)
        assertEquals(1, response.totalPages)
        verify(libraryItemRepository).findAll(pageable)
    }

    // Тест: получение пустого списка LibraryItem
    @Test
    fun `should return empty list when no library items exist`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl<LibraryItem>(emptyList(), pageable, 0)
        whenever(libraryItemRepository.findAll(pageable)).thenReturn(page)

        val response = libraryItemService.getAllLibraryItems(pageable)

        assertEquals(0, response.content.size)
        assertEquals(0, response.pageable.pageNumber)
        assertEquals(10, response.pageable.pageSize)
        assertEquals(0, response.totalElements)
        assertEquals(0, response.totalPages)
        verify(libraryItemRepository).findAll(pageable)
    }

    // Тест: проверка пагинации с несколькими LibraryItem
    @Test
    fun `should handle pagination correctly for multiple library items`() {
        val pageable = PageRequest.of(0, 1) // Одна запись на страницу
        val book1 = Book(
            id = 1L,
            title = "Book One",
            author = "Author One",
            isbn = "9781234567890",
            publishedYear = 2000
        )
        val book2 = Book(
            id = 2L,
            title = "Book Two",
            author = "Author Two",
            isbn = "9780987654321",
            publishedYear = 2001
        )
        val libraryItem1 = LibraryItem(id = 1L, book = book1, totalCopies = 2, availableCopies = 2)
        val libraryItem2 = LibraryItem(id = 2L, book = book2, totalCopies = 4, availableCopies = 4)
        val page1 = PageImpl(listOf(libraryItem1), pageable, 2)
        val page2 = PageImpl(listOf(libraryItem2), PageRequest.of(1, 1), 2)
        whenever(libraryItemRepository.findAll(PageRequest.of(0, 1))).thenReturn(page1)
        whenever(libraryItemRepository.findAll(PageRequest.of(1, 1))).thenReturn(page2)

        val response1 = libraryItemService.getAllLibraryItems(PageRequest.of(0, 1))
        val response2 = libraryItemService.getAllLibraryItems(PageRequest.of(1, 1))

        // Проверка первой страницы
        assertEquals(1, response1.content.size)
        assertEquals(libraryItem1, response1.content[0])
        assertEquals(0, response1.pageable.pageNumber)
        assertEquals(1, response1.pageable.pageSize)
        assertEquals(2, response1.totalElements)
        assertEquals(2, response1.totalPages)

        // Проверка второй страницы
        assertEquals(1, response2.content.size)
        assertEquals(libraryItem2, response2.content[0])
        assertEquals(1, response2.pageable.pageNumber)
        assertEquals(1, response2.pageable.pageSize)
        assertEquals(2, response2.totalElements)
        assertEquals(2, response2.totalPages)

        verify(libraryItemRepository).findAll(PageRequest.of(0, 1))
        verify(libraryItemRepository).findAll(PageRequest.of(1, 1))
    }


    @Test
    fun `should create library item with minimum totalCopies`() {
        val book = Book(
            id = 1L,
            title = "book title",
            author = "book author",
            isbn = "9780451524935",
            publishedYear = 1990
        )
        val libraryItem = LibraryItem(
            book = book,
            totalCopies = 1,
            availableCopies = 1
        )
        val savedLibraryItem = libraryItem.copy(id = 1L)
        whenever(bookRepository.findById(1L)).thenReturn(book)
        whenever(libraryItemRepository.save(libraryItem)).thenReturn(savedLibraryItem)

        val response = libraryItemService.createLibraryItem(1L, 1)

        assertEquals(1L, response.id)
        assertEquals(book, response.book)
        assertEquals(1, response.totalCopies)
        assertEquals(1, response.availableCopies)
        verify(bookRepository).findById(1L)
        verify(libraryItemRepository).save(libraryItem)
    }
}