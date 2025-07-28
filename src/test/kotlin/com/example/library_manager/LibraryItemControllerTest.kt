package com.example.library_manager

import com.example.library_manager.controller.dto.book.BookCreateRequest
import com.example.library_manager.controller.dto.book.BookResponse
import com.example.library_manager.controller.dto.library_item.LibraryItemRequest
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
class LibraryItemControllerTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

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
}