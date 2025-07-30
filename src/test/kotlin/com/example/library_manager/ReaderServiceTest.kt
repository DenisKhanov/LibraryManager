package com.example.library_manager

import com.example.library_manager.domain.Reader
import com.example.library_manager.repository.impl.ReaderRepository
import com.example.library_manager.service.ReaderService
import com.example.library_manager.service.exceptions.DuplicateEmailCustomException
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
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@ExtendWith(MockitoExtension::class)
class ReaderServiceTest {

    @Mock
    private lateinit var readerRepository: ReaderRepository

    @InjectMocks
    private lateinit var readerService: ReaderService

    @Test
    fun `should create reader successfully`() {
        val reader = Reader(
            id = 0L,
            name = "John Doe",
            email = "john.doe@example.com",
            registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
        )
        val savedReader = reader.copy(id = 1L)
        whenever(readerRepository.existsByEmail(reader.email)).thenReturn(false)
        whenever(readerRepository.save(reader)).thenReturn(savedReader)

        val response = readerService.createReader(reader)

        assertEquals(1L, response.id)
        assertEquals("John Doe", response.name)
        assertEquals("john.doe@example.com", response.email)
        assertEquals(reader.registeredAt, response.registeredAt)
        verify(readerRepository).existsByEmail(reader.email)
        verify(readerRepository).save(reader)
    }

    @Test
    fun `should throw DuplicateEmailCustomException when email already exists`() {
        val reader = Reader(
            id = 0L,
            name = "Jane Doe",
            email = "jane.doe@example.com",
            registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
        )
        whenever(readerRepository.existsByEmail(reader.email)).thenReturn(true)

        assertFailsWith<DuplicateEmailCustomException> {
            readerService.createReader(reader)
        }.also {
            assertEquals("Reader with email jane.doe@example.com already exists", it.message)
        }
        verify(readerRepository).existsByEmail(reader.email)
        verify(readerRepository, never()).save(org.mockito.kotlin.any())
    }

    @Test
    fun `should return paginated list of readers`() {
        val pageable = PageRequest.of(0, 10)
        val reader = Reader(
            id = 1L,
            name = "John Doe",
            email = "john.doe@example.com",
            registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
        )
        val page = PageImpl(listOf(reader), pageable, 1)
        whenever(readerRepository.findAll(pageable)).thenReturn(page)

        val response = readerService.getReaders(pageable)

        assertEquals(1, response.content.size)
        assertEquals(reader, response.content[0])
        assertEquals(0, response.pageable.pageNumber)
        assertEquals(10, response.pageable.pageSize)
        assertEquals(1, response.totalElements)
        assertEquals(1, response.totalPages)
        verify(readerRepository).findAll(pageable)
    }

    @Test
    fun `should return empty list when no readers exist`() {
        val pageable = PageRequest.of(0, 10)
        val page = PageImpl<Reader>(emptyList(), pageable, 0)
        whenever(readerRepository.findAll(pageable)).thenReturn(page)

        val response = readerService.getReaders(pageable)

        assertEquals(0, response.content.size)
        assertEquals(0, response.pageable.pageNumber)
        assertEquals(10, response.pageable.pageSize)
        assertEquals(0, response.totalElements)
        assertEquals(0, response.totalPages)
        verify(readerRepository).findAll(pageable)
    }

    @Test
    fun `should handle pagination correctly for multiple readers`() {
        val reader1 = Reader(
            id = 1L,
            name = "John Doe",
            email = "john.doe@example.com",
            registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
        )
        val reader2 = Reader(
            id = 2L,
            name = "Jane Doe",
            email = "jane.doe@example.com",
            registeredAt = LocalDateTime.of(2025, 7, 30, 13, 0)
        )
        val page1 = PageImpl(listOf(reader1), PageRequest.of(0, 1), 2)
        val page2 = PageImpl(listOf(reader2), PageRequest.of(1, 1), 2)
        whenever(readerRepository.findAll(PageRequest.of(0, 1))).thenReturn(page1)
        whenever(readerRepository.findAll(PageRequest.of(1, 1))).thenReturn(page2)

        val response1 = readerService.getReaders(PageRequest.of(0, 1))
        val response2 = readerService.getReaders(PageRequest.of(1, 1))


        assertEquals(1, response1.content.size)
        assertEquals(reader1, response1.content[0])
        assertEquals(0, response1.pageable.pageNumber)
        assertEquals(1, response1.pageable.pageSize)
        assertEquals(2, response1.totalElements)
        assertEquals(2, response1.totalPages)

        assertEquals(1, response2.content.size)
        assertEquals(reader2, response2.content[0])
        assertEquals(1, response2.pageable.pageNumber)
        assertEquals(1, response2.pageable.pageSize)
        assertEquals(2, response2.totalElements)
        assertEquals(2, response2.totalPages)

        verify(readerRepository).findAll(PageRequest.of(0, 1))
        verify(readerRepository).findAll(PageRequest.of(1, 1))
    }

    @Test
    fun `should create reader with minimal data`() {
        val reader = Reader(
            id = 0L,
            name = "A",
            email = "a@b.c",
            registeredAt = LocalDateTime.of(2025, 7, 30, 12, 0)
        )
        val savedReader = reader.copy(id = 1L)
        whenever(readerRepository.existsByEmail(reader.email)).thenReturn(false)
        whenever(readerRepository.save(reader)).thenReturn(savedReader)

        val response = readerService.createReader(reader)

        assertEquals(1L, response.id)
        assertEquals("A", response.name)
        assertEquals("a@b.c", response.email)
        assertEquals(reader.registeredAt, response.registeredAt)
        verify(readerRepository).existsByEmail(reader.email)
        verify(readerRepository).save(reader)
    }
}