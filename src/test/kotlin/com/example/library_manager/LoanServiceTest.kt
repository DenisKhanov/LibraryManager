package com.example.library_manager

import com.example.library_manager.domain.Book
import com.example.library_manager.domain.LibraryItem
import com.example.library_manager.domain.Loan
import com.example.library_manager.domain.Reader
import com.example.library_manager.repository.impl.LibraryItemRepository
import com.example.library_manager.repository.impl.LoanRepository
import com.example.library_manager.repository.impl.ReaderRepository
import com.example.library_manager.service.LoanService
import com.example.library_manager.service.exceptions.LoanAlreadyReturnedCustomException
import com.example.library_manager.service.exceptions.NotAvailableCustomException
import com.example.library_manager.service.exceptions.ResourceNotFoundCustomException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class LoanServiceTest {

    @Mock
    private lateinit var loanRepository: LoanRepository

    @Mock
    private lateinit var readerRepository: ReaderRepository

    @Mock
    private lateinit var libraryItemRepository: LibraryItemRepository

    @InjectMocks
    private lateinit var loanService: LoanService

    private val fixedLoanDate = LocalDateTime.of(2025, 7, 30, 12, 0)
    private val fixedReturnDate = LocalDateTime.of(2025, 7, 30, 12, 30)

    @Test
    fun `should create loan successfully`() {
        val reader = Reader(
            id = 1L,
            name = "John Doe",
            email = "john.doe@example.com",
            registeredAt = fixedLoanDate
        )
        val libraryItem = LibraryItem(
            id = 1L,
            book = mockBook(),
            totalCopies = 2,
            availableCopies = 2
        )
        val updatedLibraryItem = libraryItem.copy(availableCopies = 1)
        val loan = Loan(
            id = 0L,
            reader = reader,
            libraryItem = updatedLibraryItem,
            loanDate = fixedLoanDate,
            returnDate = null
        )
        val savedLoan = loan.copy(id = 1L)

        whenever(readerRepository.findById(1L)).thenReturn(reader)
        whenever(libraryItemRepository.findById(1L)).thenReturn(libraryItem)
        whenever(libraryItemRepository.save(updatedLibraryItem)).thenReturn(updatedLibraryItem)
        whenever(loanRepository.save(any())).thenReturn(savedLoan)

        val result = loanService.createLoan(1L, 1L)

        assertEquals(1L, result.id)
        assertEquals(reader, result.reader)
        assertEquals(updatedLibraryItem, result.libraryItem)
        assertEquals(fixedLoanDate, result.loanDate)
        assertEquals(null, result.returnDate)
        verify(readerRepository).findById(1L)
        verify(libraryItemRepository).findById(1L)
        verify(libraryItemRepository).save(updatedLibraryItem)
        verify(loanRepository).save(any())
    }

    @Test
    fun `should throw ResponseStatusException when reader not found`() {
        whenever(readerRepository.findById(999L)).thenReturn(null)

        assertFailsWith<ResourceNotFoundCustomException> {
            loanService.createLoan(999L, 1L)
        }.also {
            assertEquals("Reader with ID 999 not found", it.message)
        }
        verify(readerRepository).findById(999L)
        verify(libraryItemRepository, never()).findById(any())
        verify(libraryItemRepository,never()).save(any())
        verify(loanRepository,never()).save(any())
    }

    @Test
    fun `should throw ResourceNotFoundCustomException when library item not found`() {
        val reader = Reader(id = 1L, name = "John Doe", email = "john.doe@example.com", registeredAt = fixedLoanDate)
        whenever(readerRepository.findById(1L)).thenReturn(reader)
        whenever(libraryItemRepository.findById(999L)).thenReturn(null)

        assertFailsWith<ResourceNotFoundCustomException> {
            loanService.createLoan(1L, 999L)
        }.also {
            assertEquals("Library item with ID 999 not found", it.message)
        }
        verify(readerRepository).findById(1L)
        verify(libraryItemRepository).findById(999L)
        verify(libraryItemRepository,never()).save(any())
        verify(loanRepository,never()).save(any())
    }

    @Test
    fun `should throw ResponseStatusException when no available copies`() {
        val reader = Reader(id = 1L, name = "John Doe", email = "john.doe@example.com", registeredAt = fixedLoanDate)
        val libraryItem = LibraryItem(id = 1L, book = mockBook(), totalCopies = 2, availableCopies = 0)
        whenever(readerRepository.findById(1L)).thenReturn(reader)
        whenever(libraryItemRepository.findById(1L)).thenReturn(libraryItem)

        assertFailsWith<NotAvailableCustomException> {
            loanService.createLoan(1L, 1L)
        }.also {
            assertEquals("Library item with ID 1 is not available", it.message)
        }
        verify(readerRepository).findById(1L)
        verify(libraryItemRepository).findById(1L)
        verify(libraryItemRepository,never()).save(any())
        verify(loanRepository,never()).save(any())
    }

    @Test
    fun `should return loan successfully`() {
        val reader = Reader(id = 1L, name = "John Doe", email = "john.doe@example.com", registeredAt = fixedLoanDate)
        val libraryItem = LibraryItem(id = 1L, book = mockBook(), totalCopies = 2, availableCopies = 1)
        val updatedLibraryItem = libraryItem.copy(availableCopies = 2)
        val loan = Loan(id = 1L, reader = reader, libraryItem = libraryItem, loanDate = fixedLoanDate, returnDate = null)
        val updatedLoan = loan.copy(returnDate = fixedReturnDate)
        whenever(loanRepository.findById(1L)).thenReturn(loan)
        whenever(libraryItemRepository.findById(1L)).thenReturn(libraryItem)
        whenever(libraryItemRepository.save(updatedLibraryItem)).thenReturn(updatedLibraryItem)
        whenever(loanRepository.save(any())).thenReturn(updatedLoan)

        val result = loanService.returnLoan(1L)

        assertEquals(1L, result.id)
        assertEquals(reader, result.reader)
        assertEquals(libraryItem, result.libraryItem)
        assertEquals(fixedLoanDate, result.loanDate)
        assertEquals(fixedReturnDate, result.returnDate)
        verify(loanRepository).findById(1L)
        verify(libraryItemRepository).findById(1L)
        verify(libraryItemRepository).save(updatedLibraryItem)
        verify(loanRepository).save(any())
    }

    @Test
    fun `should throw ResourceNotFoundCustomException when loan not found for return`() {
        whenever(loanRepository.findById(999L)).thenReturn(null)

        assertFailsWith<ResourceNotFoundCustomException> {
            loanService.returnLoan(999L)
        }.also {
            assertEquals("Loan with ID 999 not found", it.message)
        }
        verify(loanRepository).findById(999L)
        verify(libraryItemRepository,never()).findById(any())
        verify(libraryItemRepository,never()).save(any())
        verify(loanRepository,never()).save(any())
    }

    @Test
    fun `should throw ResponseStatusException when loan already returned`() {
        val reader = Reader(id = 1L, name = "John Doe", email = "john.doe@example.com", registeredAt = fixedLoanDate)
        val libraryItem = LibraryItem(id = 1L, book = mockBook(), totalCopies = 2, availableCopies = 1)
        val loan = Loan(id = 1L, reader = reader, libraryItem = libraryItem, loanDate = fixedLoanDate, returnDate = fixedReturnDate)
        whenever(loanRepository.findById(1L)).thenReturn(loan)

        assertFailsWith<LoanAlreadyReturnedCustomException> {
            loanService.returnLoan(1L)
        }.also {
            assertEquals("Loan with ID 1 has already been returned", it.message)
        }
        verify(loanRepository).findById(1L)
        verify(libraryItemRepository,never()).findById(any())
        verify(libraryItemRepository,never()).save(any())
        verify(loanRepository,never()).save(any())
    }

    @Test
    fun `should return paginated list of loans`() {
        val reader = Reader(id = 1L, name = "John Doe", email = "john.doe@example.com", registeredAt = fixedLoanDate)
        val libraryItem = LibraryItem(id = 1L, book = mockBook(), totalCopies = 2, availableCopies = 1)
        val loan = Loan(id = 1L, reader = reader, libraryItem = libraryItem, loanDate = fixedLoanDate, returnDate = null)
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl(listOf(loan), pageable, 1)
        whenever(readerRepository.existsById(1L)).thenReturn(true)
        whenever(loanRepository.findAll(pageable)).thenReturn(page)

        val result = loanService.getLoans(pageable, 1L)

        assertEquals(1, result.content.size)
        assertEquals(loan, result.content[0])
        assertEquals(0, result.pageable.pageNumber)
        assertEquals(10, result.pageable.pageSize)
        assertEquals(1, result.totalElements)
        assertEquals(1, result.totalPages)
        verify(readerRepository).existsById(1L)
        verify(loanRepository).findAll(pageable)
    }

    @Test
    fun `should return empty list when no loans exist`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl<Loan>(emptyList(), pageable, 0)
        whenever(readerRepository.existsById(1L)).thenReturn(true)
        whenever(loanRepository.findAll(pageable)).thenReturn(page)

        val result = loanService.getLoans(pageable, 1L)

        assertEquals(0, result.content.size)
        assertEquals(0, result.pageable.pageNumber)
        assertEquals(10, result.pageable.pageSize)
        assertEquals(0, result.totalElements)
        assertEquals(0, result.totalPages)
        verify(readerRepository).existsById(1L)
        verify(loanRepository).findAll(pageable)
    }

    @Test
    fun `should handle pagination correctly for multiple loans`() {
        val reader1 = Reader(id = 1L, name = "John Doe", email = "john.doe@example.com", registeredAt = fixedLoanDate)
        val reader2 = Reader(id = 2L, name = "Jane Doe", email = "jane.doe@example.com", registeredAt = fixedLoanDate)
        val libraryItem = LibraryItem(id = 1L, book = mockBook(), totalCopies = 2, availableCopies = 1)
        val loan1 = Loan(id = 1L, reader = reader1, libraryItem = libraryItem, loanDate = fixedLoanDate, returnDate = null)
        val loan2 = Loan(id = 2L, reader = reader2, libraryItem = libraryItem, loanDate = fixedLoanDate, returnDate = null)
        val pageable1 = PageRequest.of(0, 1)
        val pageable2 = PageRequest.of(1, 1)
        val page1 = PageImpl(listOf(loan1), pageable1, 2)
        val page2 = PageImpl(listOf(loan2), pageable2, 2)
        whenever(readerRepository.existsById(1L)).thenReturn(true)
        whenever(loanRepository.findAll(pageable1)).thenReturn(page1)
        whenever(loanRepository.findAll(pageable2)).thenReturn(page2)

        val result1 = loanService.getLoans(pageable1, 1L)
        val result2 = loanService.getLoans(pageable2, 1L)


        assertEquals(1, result1.content.size)
        assertEquals(loan1, result1.content[0])
        assertEquals(0, result1.pageable.pageNumber)
        assertEquals(1, result1.pageable.pageSize)
        assertEquals(2, result1.totalElements)
        assertEquals(2, result1.totalPages)

        assertEquals(1, result2.content.size)
        assertEquals(loan2, result2.content[0])
        assertEquals(1, result2.pageable.pageNumber)
        assertEquals(1, result2.pageable.pageSize)
        assertEquals(2, result2.totalElements)
        assertEquals(2, result2.totalPages)

        verify(readerRepository, times(2)).existsById(1L)
        verify(loanRepository).findAll(pageable1)
        verify(loanRepository).findAll(pageable2)
    }

    @Test
    fun `should throw ResourceNotFoundCustomException when reader not found for getLoans`() {
        whenever(readerRepository.existsById(999L)).thenReturn(false)

        assertFailsWith<ResourceNotFoundCustomException> {
            loanService.getLoans(PageRequest.of(0, 10), 999L)
        }.also {
            assertEquals("Reader with ID 999 not found", it.message)
        }
        verify(readerRepository).existsById(999L)
        verify(loanRepository,never()).findAll(any())
    }

    private fun mockBook() = Book(
        id = 1L,
        title = "Test Book",
        author = "Test Author",
        isbn = "9781234567890",
        publishedYear = 2020
    )
}