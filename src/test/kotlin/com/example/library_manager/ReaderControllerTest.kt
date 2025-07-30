package com.example.library_manager

import com.example.library_manager.controller.dto.reader.ReaderRequest
import com.example.library_manager.repository.jpa.ReaderJpaRepository
import com.fasterxml.jackson.databind.ObjectMapper
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

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ReaderControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var readerJpaRepository: ReaderJpaRepository

    @BeforeEach
    fun setUp() {
        readerJpaRepository.deleteAll()
    }

    @Test
    fun `should create reader successfully`() {
        val readerRequest = ReaderRequest(
            name = "John Doe",
            email = "john.doe@example.com"
        )

        mockMvc.perform(
            post("/readers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readerRequest))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.name").value("John Doe"))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.registeredAt").exists())
    }

    @Test
    fun `should return 400 when creating reader with duplicate email`() {
        // Подготовка: создаем читателя в базе
        val readerRequest1 = ReaderRequest(
            name = "John Doe",
            email = "john.doe@example.com"
        )
        mockMvc.perform(
            post("/readers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readerRequest1))
        )
            .andExpect(status().isCreated)

        val readerRequest2 = ReaderRequest(
            name = "Jane Doe",
            email = "john.doe@example.com"
        )
        mockMvc.perform(
            post("/readers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readerRequest2))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.message").value("Reader with email john.doe@example.com already exists"))
    }

    @Test
    fun `should return 400 when creating reader with invalid data`() {
        val readerRequest = ReaderRequest(
            name = "",
            email = "invalid-email"
        )

        mockMvc.perform(
            post("/readers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readerRequest))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.name").value("Name must be between 2 and 100 characters"))
            .andExpect(jsonPath("$.email").value("Email must be a valid email address"))
    }

    @Test
    fun `should return paginated list of readers`() {
        // Подготовка: создаем читателя
        val readerRequest = ReaderRequest(
            name = "John Doe",
            email = "john.doe@example.com"
        )
        mockMvc.perform(
            post("/readers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readerRequest))
        )
            .andExpect(status().isCreated)

        mockMvc.perform(
            get("/readers")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON)
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content").isArray)
            .andExpect(jsonPath("$.content[0].name").value("John Doe"))
            .andExpect(jsonPath("$.content[0].email").value("john.doe@example.com"))
            .andExpect(jsonPath("$.content[0].registeredAt").exists())
            .andExpect(jsonPath("$.pageable.pageNumber").value(0))
            .andExpect(jsonPath("$.pageable.pageSize").value(10))
            .andExpect(jsonPath("$.totalElements").value(1))
            .andExpect(jsonPath("$.totalPages").value(1))
    }

    @Test
    fun `should return empty list when no readers exist`() {
        mockMvc.perform(
            get("/readers")
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
    fun `should handle pagination correctly for multiple readers`() {
        // Подготовка: создаем двух читателей
        val readerRequest1 = ReaderRequest(
            name = "John Doe",
            email = "john.doe@example.com"
        )
        val readerRequest2 = ReaderRequest(
            name = "Jane Doe",
            email = "jane.doe@example.com"
        )

        mockMvc.perform(
            post("/readers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readerRequest1))
        )
            .andExpect(status().isCreated)

        mockMvc.perform(
            post("/readers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(readerRequest2))
        )
            .andExpect(status().isCreated)

        // Запрашиваем первую страницу с size=1
        mockMvc.perform(
            get("/readers")
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
            get("/readers")
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