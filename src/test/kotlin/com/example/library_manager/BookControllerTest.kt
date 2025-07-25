package com.example.library_manager

import com.example.library_manager.controller.dto.book.BookCreateRequest
import com.example.library_manager.controller.dto.book.BookUpdateRequest
import com.fasterxml.jackson.databind.ObjectMapper
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
class BookControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `should create book successfully`() {
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
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.title").value("1984"))
            .andExpect(jsonPath("$.author").value("George Orwell"))
            .andExpect(jsonPath("$.isbn").value("9780451524935"))
            .andExpect(jsonPath("$.publishedYear").value(1949))
    }

    @Test
    fun `should return 400 for invalid book request`() {
        val invalidRequest = BookCreateRequest(
            title = "", // Пустое название
            author = "George Orwell",
            isbn = "invalid-isbn", // Некорректный ISBN
            publishedYear = 0 // Некорректный год
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

        // Создаём первую книгу
        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // Пытаемся создать книгу с тем же ISBN
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
        // Создаём две книги
        val request1 = BookCreateRequest(
            title = "1984",
            author = "George Orwell",
            isbn = "9780451524935",
            publishedYear = 1949
        )
        val request2 = BookCreateRequest(
            title = "Animal Farm",
            author = "George Orwell",
            isbn = "9780451526342",
            publishedYear = 1945
        )

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request1))
        )
            .andExpect(status().isCreated)

        mockMvc.perform(
            post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request2))
        )
            .andExpect(status().isCreated)

        // Получаем список с пагинацией
        mockMvc.perform(
            get("/books")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "title,asc")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.content[0].title").value("1984"))
            .andExpect(jsonPath("$.content[1].title").value("Animal Farm"))
            .andExpect(jsonPath("$.pageable.pageNumber").value(0))
            .andExpect(jsonPath("$.pageable.pageSize").value(10))
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.totalPages").value(1))
    }

    @Test
    fun `should update book successfully`() {
        // Создаём книгу
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

        // Обновляем книгу
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
            .andExpect(status().isOk)
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
            .andExpect(jsonPath("$.message").value("Book with id 999 not found"))
    }

    @Test
    fun `should delete book successfully`() {
        // Создаём книгу
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

        // Удаляем книгу
        mockMvc.perform(
            delete("/books/$bookId")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.message").value("Book with id $bookId deleted successfully"))
    }

    @Test
    fun `should return 404 for deleting non-existing book`() {
        mockMvc.perform(
            delete("/books/999")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.message").value("Book with id 999 not found"))
    }
}