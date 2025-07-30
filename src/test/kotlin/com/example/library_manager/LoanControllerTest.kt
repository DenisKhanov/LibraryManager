package com.example.library_manager

import com.example.library_manager.controller.dto.loan.LoanRequest
import com.example.library_manager.repository.jpa.BookJpaRepository
import com.example.library_manager.repository.jpa.LibraryItemJpaRepository
import com.example.library_manager.repository.jpa.LoanJpaRepository
import com.example.library_manager.repository.jpa.ReaderJpaRepository
import com.example.library_manager.repository.jpa.entity.book.BookEntity
import com.example.library_manager.repository.jpa.entity.library_item.LibraryItemEntity
import com.example.library_manager.repository.jpa.entity.loan.LoanEntity
import com.example.library_manager.repository.jpa.entity.reader.ReaderEntity
import com.example.library_manager.repository.mapper.toDomain
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoanControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var loanJpaRepository: LoanJpaRepository

    @Autowired
    private lateinit var readerJpaRepository: ReaderJpaRepository

    @Autowired
    private lateinit var libraryItemJpaRepository: LibraryItemJpaRepository

    @Autowired
    private lateinit var bookJpaRepository: BookJpaRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @BeforeEach
    fun setUp() {
        loanJpaRepository.deleteAll()
        libraryItemJpaRepository.deleteAll()
        readerJpaRepository.deleteAll()
        bookJpaRepository.deleteAll()
        entityManager.flush()
        entityManager.clear()
    }

    @Test
    fun `should create loan successfully`() {
        val savedBookEntity = bookJpaRepository.save(
            BookEntity(
                title = "Test Book",
                author = "Test Author",
                isbn = "9781234567890",
                publishedYear = 2020
            )
        )
        entityManager.flush()
        assertNotNull(savedBookEntity.id, "BookEntity ID should not be null")
        assertEquals("Test Book", savedBookEntity.title, "BookEntity title should match")
        assertEquals("Test Author", savedBookEntity.author, "BookEntity author should match")
        assertEquals("9781234567890", savedBookEntity.isbn, "BookEntity ISBN should match")
        assertEquals(2020, savedBookEntity.publishedYear, "BookEntity publishedYear should match")
        println("Saved BookEntity: $savedBookEntity")

        val savedLibraryItemEntity = libraryItemJpaRepository.save(
            LibraryItemEntity(
                bookEntity = savedBookEntity,
                totalCopies = 2,
                availableCopies = 2
            )
        )
        entityManager.flush()
        assertNotNull(savedLibraryItemEntity.id, "LibraryItemEntity ID should not be null")
        assertEquals(savedBookEntity, savedLibraryItemEntity.bookEntity, "LibraryItemEntity book should match")
        assertEquals(2, savedLibraryItemEntity.totalCopies, "LibraryItemEntity totalCopies should match")
        assertEquals(2, savedLibraryItemEntity.availableCopies, "LibraryItemEntity availableCopies should match")
        println("Saved LibraryItemEntity: $savedLibraryItemEntity")

        val savedReaderEntity = readerJpaRepository.save(
            ReaderEntity(
                name = "John Doe",
                email = "john.doe@example.com",
                registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
            )
        )
        entityManager.flush()
        assertNotNull(savedReaderEntity.id, "ReaderEntity ID should not be null")
        assertEquals("John Doe", savedReaderEntity.name, "ReaderEntity name should match")
        assertEquals("john.doe@example.com", savedReaderEntity.email, "ReaderEntity email should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedReaderEntity.registeredAt,
            "ReaderEntity registeredAt should match"
        )
        println("Saved ReaderEntity: $savedReaderEntity")

        val loanRequest = LoanRequest(
            readerId = savedReaderEntity.id ?: throw IllegalStateException("ReaderEntity ID cannot be null"),
            libraryItemId = savedLibraryItemEntity.id ?: throw IllegalStateException("LibraryItemEntity ID cannot be null")
        )
        println("LoanRequest: $loanRequest")
        println("Request JSON: ${objectMapper.writeValueAsString(loanRequest)}")

        mockMvc.perform(
            post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.readerId").value(loanRequest.readerId))
            .andExpect(jsonPath("$.libraryItemId").value(loanRequest.libraryItemId))
            .andExpect(jsonPath("$.loanDate").exists())
            .andExpect(jsonPath("$.returnDate").doesNotExist())

        val updatedLibraryItem = libraryItemJpaRepository.findById(loanRequest.libraryItemId).get().toDomain()
        assertEquals(1, updatedLibraryItem.availableCopies, "LibraryItem availableCopies should be decremented to 1")
    }

    @Test
    fun `should return 404 when creating loan with non-existing reader`() {
        val loanRequest = LoanRequest(
            readerId = 999L,
            libraryItemId = 1L
        )
        println("LoanRequest: $loanRequest")
        println("Request JSON: ${objectMapper.writeValueAsString(loanRequest)}")

        mockMvc.perform(
            post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Reader with ID 999 not found"))
    }

    @Test
    fun `should return 404 when creating loan with non-existing library item`() {
        val savedReaderEntity = readerJpaRepository.save(
            ReaderEntity(
                name = "John Doe",
                email = "john.doe@example.com",
                registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
            )
        )
        entityManager.flush()
        assertNotNull(savedReaderEntity.id, "ReaderEntity ID should not be null")
        assertEquals("John Doe", savedReaderEntity.name, "ReaderEntity name should match")
        assertEquals("john.doe@example.com", savedReaderEntity.email, "ReaderEntity email should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedReaderEntity.registeredAt,
            "ReaderEntity registeredAt should match"
        )
        println("Saved ReaderEntity: $savedReaderEntity")

        val loanRequest = LoanRequest(
            readerId = savedReaderEntity.id ?: throw IllegalStateException("ReaderEntity ID cannot be null"),
            libraryItemId = 999L
        )
        println("LoanRequest: $loanRequest")
        println("Request JSON: ${objectMapper.writeValueAsString(loanRequest)}")

        mockMvc.perform(
            post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Library item with ID 999 not found"))
    }

    @Test
    fun `should return 400 when creating loan with no available copies`() {
        val savedBookEntity = bookJpaRepository.save(
            BookEntity(
                title = "Test Book",
                author = "Test Author",
                isbn = "9781234567890",
                publishedYear = 2020
            )
        )
        entityManager.flush()
        assertNotNull(savedBookEntity.id, "BookEntity ID should not be null")
        assertEquals("Test Book", savedBookEntity.title, "BookEntity title should match")
        assertEquals("Test Author", savedBookEntity.author, "BookEntity author should match")
        assertEquals("9781234567890", savedBookEntity.isbn, "BookEntity ISBN should match")
        assertEquals(2020, savedBookEntity.publishedYear, "BookEntity publishedYear should match")

        val savedLibraryItemEntity = libraryItemJpaRepository.save(
            LibraryItemEntity(
                bookEntity = savedBookEntity,
                totalCopies = 1,
                availableCopies = 0
            )
        )
        entityManager.flush()
        assertNotNull(savedLibraryItemEntity.id, "LibraryItemEntity ID should not be null")
        assertEquals(savedBookEntity, savedLibraryItemEntity.bookEntity, "LibraryItemEntity book should match")
        assertEquals(1, savedLibraryItemEntity.totalCopies, "LibraryItemEntity totalCopies should match")
        assertEquals(0, savedLibraryItemEntity.availableCopies, "LibraryItemEntity availableCopies should match")

        val savedReaderEntity = readerJpaRepository.save(
            ReaderEntity(
                name = "John Doe",
                email = "john.doe@example.com",
                registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
            )
        )
        entityManager.flush()
        assertNotNull(savedReaderEntity.id, "ReaderEntity ID should not be null")
        assertEquals("John Doe", savedReaderEntity.name, "ReaderEntity name should match")
        assertEquals("john.doe@example.com", savedReaderEntity.email, "ReaderEntity email should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedReaderEntity.registeredAt,
            "ReaderEntity registeredAt should match"
        )

        val loanRequest = LoanRequest(
            readerId = savedReaderEntity.id ?: throw IllegalStateException("ReaderEntity ID cannot be null"),
            libraryItemId = savedLibraryItemEntity.id ?: throw IllegalStateException("LibraryItemEntity ID cannot be null")
        )

        mockMvc.perform(
            post("/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loanRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Library item with ID ${savedLibraryItemEntity.id} is not available"))
    }

    @Test
    fun `should return loan successfully`() {
        val savedBookEntity = bookJpaRepository.save(
            BookEntity(
                title = "Test Book",
                author = "Test Author",
                isbn = "9781234567890",
                publishedYear = 2020
            )
        )
        entityManager.flush()
        assertNotNull(savedBookEntity.id, "BookEntity ID should not be null")
        assertEquals("Test Book", savedBookEntity.title, "BookEntity title should match")
        assertEquals("Test Author", savedBookEntity.author, "BookEntity author should match")
        assertEquals("9781234567890", savedBookEntity.isbn, "BookEntity ISBN should match")
        assertEquals(2020, savedBookEntity.publishedYear, "BookEntity publishedYear should match")

        val savedLibraryItemEntity = libraryItemJpaRepository.save(
            LibraryItemEntity(
                bookEntity = savedBookEntity,
                totalCopies = 2,
                availableCopies = 1
            )
        )
        entityManager.flush()
        assertNotNull(savedLibraryItemEntity.id, "LibraryItemEntity ID should not be null")
        assertEquals(savedBookEntity, savedLibraryItemEntity.bookEntity, "LibraryItemEntity book should match")
        assertEquals(2, savedLibraryItemEntity.totalCopies, "LibraryItemEntity totalCopies should match")
        assertEquals(1, savedLibraryItemEntity.availableCopies, "LibraryItemEntity availableCopies should match")

        val savedReaderEntity = readerJpaRepository.save(
            ReaderEntity(
                name = "John Doe",
                email = "john.doe@example.com",
                registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
            )
        )
        entityManager.flush()
        assertNotNull(savedReaderEntity.id, "ReaderEntity ID should not be null")
        assertEquals("John Doe", savedReaderEntity.name, "ReaderEntity name should match")
        assertEquals("john.doe@example.com", savedReaderEntity.email, "ReaderEntity email should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedReaderEntity.registeredAt,
            "ReaderEntity registeredAt should match"
        )

        val savedLoanEntity = loanJpaRepository.save(
            LoanEntity(
                readerEntity = savedReaderEntity,
                libraryItemEntity = savedLibraryItemEntity,
                loanDate = LocalDateTime.of(2025, 7, 30, 12, 0),
                returnDate = null
            )
        )
        entityManager.flush()
        assertNotNull(savedLoanEntity.id, "LoanEntity ID should not be null")
        assertEquals(savedReaderEntity, savedLoanEntity.readerEntity, "LoanEntity reader should match")
        assertEquals(savedLibraryItemEntity, savedLoanEntity.libraryItemEntity, "LoanEntity libraryItem should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedLoanEntity.loanDate,
            "LoanEntity loanDate should match"
        )
        assertNull(savedLoanEntity.returnDate, "LoanEntity returnDate should be null")

        mockMvc.perform(
            post("/loans/${savedLoanEntity.id}/return")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(savedLoanEntity.id))
            .andExpect(jsonPath("$.readerId").value(savedReaderEntity.id))
            .andExpect(jsonPath("$.libraryItemId").value(savedLibraryItemEntity.id))
            .andExpect(jsonPath("$.loanDate").exists())
            .andExpect(jsonPath("$.returnDate").exists())

        val updatedLibraryItem = libraryItemJpaRepository.findById(savedLibraryItemEntity.id!!).get().toDomain()
        assertEquals(2, updatedLibraryItem.availableCopies, "LibraryItem availableCopies should be incremented to 2")
    }

    @Test
    fun `should return 404 when loan not found`() {
      val loanId = 999L

        mockMvc.perform(
            post("/loans/$loanId/return")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Loan with ID $loanId not found"))
    }

    @Test
    fun `should return 400 when returning already returned loan`() {
        val savedBookEntity = bookJpaRepository.save(
            BookEntity(
                title = "Test Book",
                author = "Test Author",
                isbn = "9781234567890",
                publishedYear = 2020
            )
        )
        entityManager.flush()
        assertNotNull(savedBookEntity.id, "BookEntity ID should not be null")
        assertEquals("Test Book", savedBookEntity.title, "BookEntity title should match")
        assertEquals("Test Author", savedBookEntity.author, "BookEntity author should match")
        assertEquals("9781234567890", savedBookEntity.isbn, "BookEntity ISBN should match")
        assertEquals(2020, savedBookEntity.publishedYear, "BookEntity publishedYear should match")

        val savedLibraryItemEntity = libraryItemJpaRepository.save(
            LibraryItemEntity(
                bookEntity = savedBookEntity,
                totalCopies = 2,
                availableCopies = 1
            )
        )
        entityManager.flush()
        assertNotNull(savedLibraryItemEntity.id, "LibraryItemEntity ID should not be null")
        assertEquals(savedBookEntity, savedLibraryItemEntity.bookEntity, "LibraryItemEntity book should match")
        assertEquals(2, savedLibraryItemEntity.totalCopies, "LibraryItemEntity totalCopies should match")
        assertEquals(1, savedLibraryItemEntity.availableCopies, "LibraryItemEntity availableCopies should match")

        val savedReaderEntity = readerJpaRepository.save(
            ReaderEntity(
                name = "John Doe",
                email = "john.doe@example.com",
                registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
            )
        )
        entityManager.flush()
        assertNotNull(savedReaderEntity.id, "ReaderEntity ID should not be null")
        assertEquals("John Doe", savedReaderEntity.name, "ReaderEntity name should match")
        assertEquals("john.doe@example.com", savedReaderEntity.email, "ReaderEntity email should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedReaderEntity.registeredAt,
            "ReaderEntity registeredAt should match"
        )

        val savedLoanEntity = loanJpaRepository.save(
            LoanEntity(
                readerEntity = savedReaderEntity,
                libraryItemEntity = savedLibraryItemEntity,
                loanDate = LocalDateTime.of(2025, 7, 30, 12, 0),
                returnDate = LocalDateTime.of(2025, 7, 30, 12, 30)
            )
        )
        entityManager.flush()
        assertNotNull(savedLoanEntity.id, "LoanEntity ID should not be null")
        assertEquals(savedReaderEntity, savedLoanEntity.readerEntity, "LoanEntity reader should match")
        assertEquals(savedLibraryItemEntity, savedLoanEntity.libraryItemEntity, "LoanEntity libraryItem should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedLoanEntity.loanDate,
            "LoanEntity loanDate should match"
        )
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 30),
            savedLoanEntity.returnDate,
            "LoanEntity returnDate should match"
        )

        mockMvc.perform(
            post("/loans/${savedLoanEntity.id}/return")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Loan with ID ${savedLoanEntity.id} has already been returned"))
    }

    @Test
    fun `should return paginated list of loans`() {
        val savedBookEntity = bookJpaRepository.save(
            BookEntity(
                title = "Test Book",
                author = "Test Author",
                isbn = "9781234567890",
                publishedYear = 2020
            )
        )
        entityManager.flush()
        assertNotNull(savedBookEntity.id, "BookEntity ID should not be null")
        assertEquals("Test Book", savedBookEntity.title, "BookEntity title should match")
        assertEquals("Test Author", savedBookEntity.author, "BookEntity author should match")
        assertEquals("9781234567890", savedBookEntity.isbn, "BookEntity ISBN should match")
        assertEquals(2020, savedBookEntity.publishedYear, "BookEntity publishedYear should match")

        val savedLibraryItemEntity = libraryItemJpaRepository.save(
            LibraryItemEntity(
                bookEntity = savedBookEntity,
                totalCopies = 2,
                availableCopies = 1
            )
        )
        entityManager.flush()
        assertNotNull(savedLibraryItemEntity.id, "LibraryItemEntity ID should not be null")
        assertEquals(savedBookEntity, savedLibraryItemEntity.bookEntity, "LibraryItemEntity book should match")
        assertEquals(2, savedLibraryItemEntity.totalCopies, "LibraryItemEntity totalCopies should match")
        assertEquals(1, savedLibraryItemEntity.availableCopies, "LibraryItemEntity availableCopies should match")

        val savedReaderEntity = readerJpaRepository.save(
            ReaderEntity(
                name = "John Doe",
                email = "john.doe@example.com",
                registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
            )
        )
        entityManager.flush()
        assertNotNull(savedReaderEntity.id, "ReaderEntity ID should not be null")
        assertEquals("John Doe", savedReaderEntity.name, "ReaderEntity name should match")
        assertEquals("john.doe@example.com", savedReaderEntity.email, "ReaderEntity email should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedReaderEntity.registeredAt,
            "ReaderEntity registeredAt should match"
        )

        val savedLoanEntity = loanJpaRepository.save(
            LoanEntity(
                readerEntity = savedReaderEntity,
                libraryItemEntity = savedLibraryItemEntity,
                loanDate = LocalDateTime.of(2025, 7, 30, 12, 0),
                returnDate = null
            )
        )
        entityManager.flush()
        assertNotNull(savedLoanEntity.id, "LoanEntity ID should not be null")
        assertEquals(savedReaderEntity, savedLoanEntity.readerEntity, "LoanEntity reader should match")
        assertEquals(savedLibraryItemEntity, savedLoanEntity.libraryItemEntity, "LoanEntity libraryItem should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedLoanEntity.loanDate,
            "LoanEntity loanDate should match"
        )
        assertNull(savedLoanEntity.returnDate, "LoanEntity returnDate should be null")

        mockMvc.perform(
            get("/loans/readers/${savedReaderEntity.id}/loans")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].id").value(savedLoanEntity.id))
            .andExpect(jsonPath("$.content[0].readerId").value(savedReaderEntity.id))
            .andExpect(jsonPath("$.content[0].libraryItemId").value(savedLibraryItemEntity.id))
            .andExpect(jsonPath("$.content[0].loanDate").exists())
            .andExpect(jsonPath("$.content[0].returnDate").doesNotExist())
            .andExpect(jsonPath("$.pageable.pageNumber").value(0))
            .andExpect(jsonPath("$.pageable.pageSize").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
    }

    @Test
    fun `should return empty list when no loans exist`() {
        val savedReaderEntity = readerJpaRepository.save(
            ReaderEntity(
                name = "John Doe",
                email = "john.doe@example.com",
                registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
            )
        )
        entityManager.flush()
        assertNotNull(savedReaderEntity.id, "ReaderEntity ID should not be null")
        assertEquals("John Doe", savedReaderEntity.name, "ReaderEntity name should match")
        assertEquals("john.doe@example.com", savedReaderEntity.email, "ReaderEntity email should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedReaderEntity.registeredAt,
            "ReaderEntity registeredAt should match"
        )

        mockMvc.perform(
            get("/loans/readers/${savedReaderEntity.id}/loans")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isEmpty)
            .andExpect(jsonPath("$.pageable.pageNumber").value(0))
            .andExpect(jsonPath("$.pageable.pageSize").value(10))
            .andExpect(jsonPath("$.totalElements").value(0))
            .andExpect(jsonPath("$.totalPages").value(0))
    }

    @Test
    fun `should handle pagination correctly for multiple loans`() {
        val savedBookEntity = bookJpaRepository.save(
            BookEntity(
                title = "Test Book",
                author = "Test Author",
                isbn = "9781234567890",
                publishedYear = 2020
            )
        )
        entityManager.flush()
        assertNotNull(savedBookEntity.id, "BookEntity ID should not be null")
        assertEquals("Test Book", savedBookEntity.title, "BookEntity title should match")
        assertEquals("Test Author", savedBookEntity.author, "BookEntity author should match")
        assertEquals("9781234567890", savedBookEntity.isbn, "BookEntity ISBN should match")
        assertEquals(2020, savedBookEntity.publishedYear, "BookEntity publishedYear should match")

        val savedLibraryItemEntity = libraryItemJpaRepository.save(
            LibraryItemEntity(
                bookEntity = savedBookEntity,
                totalCopies = 2,
                availableCopies = 2
            )
        )
        entityManager.flush()
        assertNotNull(savedLibraryItemEntity.id, "LibraryItemEntity ID should not be null")
        assertEquals(savedBookEntity, savedLibraryItemEntity.bookEntity, "LibraryItemEntity book should match")
        assertEquals(2, savedLibraryItemEntity.totalCopies, "LibraryItemEntity totalCopies should match")
        assertEquals(2, savedLibraryItemEntity.availableCopies, "LibraryItemEntity availableCopies should match")

        val savedReaderEntity = readerJpaRepository.save(
            ReaderEntity(
                name = "John Doe",
                email = "john.doe@example.com",
                registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
            )
        )
        entityManager.flush()
        assertNotNull(savedReaderEntity.id, "ReaderEntity ID should not be null")
        assertEquals("John Doe", savedReaderEntity.name, "ReaderEntity name should match")
        assertEquals("john.doe@example.com", savedReaderEntity.email, "ReaderEntity email should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedReaderEntity.registeredAt,
            "ReaderEntity registeredAt should match"
        )

        val savedLoanEntity1 = loanJpaRepository.save(
            LoanEntity(
                readerEntity = savedReaderEntity,
                libraryItemEntity = savedLibraryItemEntity,
                loanDate = LocalDateTime.of(2025, 7, 30, 12, 0),
                returnDate = null
            )
        )
        entityManager.flush()
        assertNotNull(savedLoanEntity1.id, "LoanEntity1 ID should not be null")
        assertEquals(savedReaderEntity, savedLoanEntity1.readerEntity, "LoanEntity1 reader should match")
        assertEquals(savedLibraryItemEntity, savedLoanEntity1.libraryItemEntity, "LoanEntity1 libraryItem should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 0),
            savedLoanEntity1.loanDate,
            "LoanEntity1 loanDate should match"
        )
        assertNull(savedLoanEntity1.returnDate, "LoanEntity1 returnDate should be null")

        val savedLoanEntity2 = loanJpaRepository.save(
            LoanEntity(
                readerEntity = savedReaderEntity,
                libraryItemEntity = savedLibraryItemEntity,
                loanDate = LocalDateTime.of(2025, 7, 30, 12, 1),
                returnDate = null
            )
        )
        entityManager.flush()
        assertNotNull(savedLoanEntity2.id, "LoanEntity2 ID should not be null")
        assertEquals(savedReaderEntity, savedLoanEntity2.readerEntity, "LoanEntity2 reader should match")
        assertEquals(savedLibraryItemEntity, savedLoanEntity2.libraryItemEntity, "LoanEntity2 libraryItem should match")
        assertEquals(
            LocalDateTime.of(2025, 7, 30, 12, 1),
            savedLoanEntity2.loanDate,
            "LoanEntity2 loanDate should match"
        )
        assertNull(savedLoanEntity2.returnDate, "LoanEntity2 returnDate should be null")

        // Запрашиваем первую страницу
        mockMvc.perform(
            get("/loans/readers/${savedReaderEntity.id}/loans")
                .param("page", "0")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.pageable.pageNumber").value(0))
            .andExpect(jsonPath("$.pageable.pageSize").value(1))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))

        // Запрашиваем вторую страницу
        mockMvc.perform(
            get("/loans/readers/${savedReaderEntity.id}/loans")
                .param("page", "1")
                .param("size", "1")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.pageable.pageNumber").value(1))
            .andExpect(jsonPath("$.pageable.pageSize").value(1))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
    }

    @Test
    fun `should return 404 when getting loans for non-existing reader`() {
        mockMvc.perform(
            get("/loans/readers/999/loans")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Reader with ID 999 not found"))
    }
}