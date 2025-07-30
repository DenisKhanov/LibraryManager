package com.example.library_manager

import com.example.library_manager.controller.dto.book.BookCreateRequest
import com.example.library_manager.controller.dto.book.BookResponse
import com.example.library_manager.controller.dto.library_item.LibraryItemRequest
import com.example.library_manager.repository.jpa.LibraryItemJpaRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LibraryItemControllerTest {

    @Autowired
    private lateinit var libraryItemJpaRepository: LibraryItemJpaRepository

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper


    @BeforeEach
    fun setUp() {
        libraryItemJpaRepository.deleteAll()
    }

    @Test
    fun `should create item successfully`() {
        val bookRequest = BookCreateRequest(
            title = "1984",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1949
        )

       val result = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest))
        )
            .andExpect { status().isCreated }
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("1984"))
            .andExpect(jsonPath("$.author").value("George Orwell"))
            .andExpect(jsonPath("$.isbn").value("9780451524935"))
            .andExpect(jsonPath("$.publishedYear").value(1949))
           .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, BookResponse::class.java)
        val bookId = response.id

        val itemRequest = LibraryItemRequest(
            bookId = bookId,
            totalCopies = 5,
        )

        mockMvc.perform(
            post("/library-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.book.title").value("1984"))
            .andExpect(jsonPath("$.book.author").value("George Orwell"))
            .andExpect(jsonPath("$.book.isbn").value("9780451524935"))
            .andExpect(jsonPath("$.book.publishedYear").value(1949))
            .andExpect(jsonPath("$.totalCopies").value(5))
            .andExpect(jsonPath("$.availableCopies").value(5))
    }

    @Test
    fun `should return 404 when creating library item with non-existent bookId`() {
        val itemRequest = LibraryItemRequest(
            bookId = 999L,
            totalCopies = 5
        )

        mockMvc.perform(
            post("/library-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemRequest))
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Book with ID 999 not found"))
    }

    @Test
    fun `should return 400 when creating library item with invalid totalCopies`() {
        val bookRequest = BookCreateRequest(
            title = "Dune",
            author = "Frank Herbert",
            isbn = "9780441172719",
            publishedYear = 1965
        )

        val result = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, BookResponse::class.java)
        val bookId = response.id

        val itemRequest = LibraryItemRequest(
            bookId = bookId,
            totalCopies = 0
        )

        mockMvc.perform(
            post("/library-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.totalCopies").value("Total copies must be positive"))
    }

    @Test
    fun `should return paginated list of library items`() {
        // Создаем книгу
        val bookRequest = BookCreateRequest(
            title = "The Hobbit",
            author = "J.R.R. Tolkien",
            isbn = "9780547928227",
            publishedYear = 1937
        )

        val result = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bookRequest))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val response = objectMapper.readValue(result.response.contentAsString, BookResponse::class.java)
        val bookId = response.id


        val itemRequest = LibraryItemRequest(
            bookId = bookId,
            totalCopies = 3
        )

        mockMvc.perform(
            post("/library-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(itemRequest))
        )
            .andExpect(status().isCreated)


        mockMvc.perform(
            get("/library-items")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].book.title").value("The Hobbit"))
            .andExpect(jsonPath("$.content[0].totalCopies").value(3))
            .andExpect(jsonPath("$.content[0].availableCopies").value(3))
            .andExpect(jsonPath("$.pageable.pageNumber").value(0))
            .andExpect(jsonPath("$.pageable.pageSize").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
    }

    @Test
    fun `should return empty list when no library items exist`() {
        mockMvc.perform(
            get("/library-items")
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
    fun `should handle pagination correctly for multiple library items`() {

        val book1Request = BookCreateRequest(
            title = "Book One",
            author = "Author One",
            isbn = "9781234567890",
            publishedYear = 2000
        )
        val book2Request = BookCreateRequest(
            title = "Book Two",
            author = "Author Two",
            isbn = "9780987654321",
            publishedYear = 2001
        )

        val book1Result = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book1Request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val book2Result = mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(book2Request))
        )
            .andExpect(status().isCreated)
            .andReturn()

        val book1Response = objectMapper.readValue(book1Result.response.contentAsString, BookResponse::class.java)
        val book2Response = objectMapper.readValue(book2Result.response.contentAsString, BookResponse::class.java)


        val item1Request = LibraryItemRequest(bookId = book1Response.id, totalCopies = 2)
        val item2Request = LibraryItemRequest(bookId = book2Response.id, totalCopies = 4)

        mockMvc.perform(
            post("/library-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item1Request))
        )
            .andExpect(status().isCreated)

        mockMvc.perform(
            post("/library-items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(item2Request))
        )
            .andExpect(status().isCreated)


        mockMvc.perform(
            get("/library-items")
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


        mockMvc.perform(
            get("/library-items")
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
}