package com.example.library_manager

import com.example.library_manager.controller.dto.book.BookCreateRequest
import com.example.library_manager.controller.dto.book.BookResponse
import com.example.library_manager.controller.dto.book.BookUpdateRequest
import com.example.library_manager.repository.jpa.BookJpaRepository
import com.example.library_manager.repository.jpa.entity.book.BookEntity
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var bookJpaRepository: BookJpaRepository

    @BeforeEach
    fun setUp() {
        bookJpaRepository.deleteAll()
    }

    @Test
    fun `should create book successfully`() {
        val request = BookCreateRequest(
            title = "1984",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1949
        )

        val result = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("1984"))
            .andExpect(jsonPath("$.author").value("George Orwell"))
            .andExpect(jsonPath("$.isbn").value("9780451524935"))
            .andExpect(jsonPath("$.publishedYear").value(1949))
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, BookResponse::class.java)
        val bookId = response.id

        val savedBook = bookJpaRepository.findByIdOrNull(bookId)
        assertNotNull(savedBook, "Book should be saved in the database")
        assertEquals(request.title, savedBook.title)
        assertEquals(request.author, savedBook.author)
        assertEquals(request.isbn, savedBook.isbn)
        assertEquals(request.publishedYear, savedBook.publishedYear)
    }

    @Test
    fun `should return 400 for invalid book request`() {
        val invalidRequest = BookCreateRequest(
            title = "",
            author = "George Orwell",
            isbn = "invalid-isbn",
            publishedYear = 0
        )

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.title").value("Title cannot be empty"))
            .andExpect(jsonPath("$.isbn").value("Invalid ISBN"))
            .andExpect(jsonPath("$.publishedYear").value("Publication year must be positive"))
    }

    @Test
    fun `should return 400 for duplicate ISBN`() {
        val request = BookCreateRequest(
            title = "1984",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1949
        )

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        val duplicateRequest = BookCreateRequest(
            title = "Animal Farm",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1945
        )

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(duplicateRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Book with ISBN 9780451524935 already exists"))
    }

    @Test
    fun `should get paginated list of books`() {

        val book1 = BookEntity(
            title = "1984",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1949
        )
        val book2 = BookEntity(
            title = "Animal Farm",
            author = "George Orwell",
            isbn = "9780451526342",
            publishedYear = 1945
        )

        val savedBook1 = bookJpaRepository.save(book1)
        val savedBook2 = bookJpaRepository.save(book2)

        mockMvc.perform(
            get("/books")
                .param("page", "0")
                .param("size", "1")
                .param("sort", "title,asc")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(savedBook1.id))
            .andExpect(jsonPath("$.content[0].title").value("1984"))
            .andExpect(jsonPath("$.content[0].author").value("George Orwell"))
            .andExpect(jsonPath("$.content[0].isbn").value("9780451524935"))
            .andExpect(jsonPath("$.content[0].publishedYear").value(1949))
            .andExpect(jsonPath("$.pageable.pageNumber").value(0))
            .andExpect(jsonPath("$.pageable.pageSize").value(1))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))

        mockMvc.perform(
            get("/books")
                .param("page", "1")
                .param("size", "1")
                .param("sort", "title,asc")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(1))
            .andExpect(jsonPath("$.content[0].id").value(savedBook2.id))
            .andExpect(jsonPath("$.content[0].title").value("Animal Farm"))
            .andExpect(jsonPath("$.content[0].author").value("George Orwell"))
            .andExpect(jsonPath("$.content[0].isbn").value("9780451526342"))
            .andExpect(jsonPath("$.content[0].publishedYear").value(1945))
            .andExpect(jsonPath("$.pageable.pageNumber").value(1))
            .andExpect(jsonPath("$.pageable.pageSize").value(1))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))


        mockMvc.perform(
            get("/books")
                .param("page", "2")
                .param("size", "1")
                .param("sort", "title,asc")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(0))
            .andExpect(jsonPath("$.pageable.pageNumber").value(2))
            .andExpect(jsonPath("$.pageable.pageSize").value(1))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
    }

    @Test
    fun `should update book successfully`() {

        val createRequest = BookCreateRequest(
            title = "1984",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1949
        )
        val createResponse = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
            .let { objectMapper.readValue(it, Map::class.java) }

        val bookId = createResponse["id"].toString()

        val updateRequest = BookUpdateRequest(
            id= bookId.toLong(),
            title = "1984 (New Edition)",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1950
        )

        mockMvc.perform(
            put("/books/$bookId")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNoContent)
    }

    @Test
    fun `should return 404 for updating non-existing book`() {
        val updateRequest = BookUpdateRequest(
            id = 999,
            title = "1984 (New Edition)",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1950,
        )

        mockMvc.perform(
            put("/books/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Book with ID 999 not found"))
    }

    @Test
    fun `should delete book successfully`() {

        val createRequest = BookCreateRequest(
            title = "1984",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1949
        )
        val createResponse = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andExpect(status().isCreated)
            .andReturn().response.contentAsString
            .let { objectMapper.readValue(it, Map::class.java) }

        val bookId = createResponse["id"].toString()

        mockMvc.perform(
            delete("/books/$bookId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `should return 404 for deleting non-existing book`() {
        mockMvc.perform(
            delete("/books/999")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Book with ID 999 not found"))
    }
}