package com.example.library_manager.controller

import com.example.library_manager.controller.dto.book.BookCreateRequest
import com.example.library_manager.controller.dto.book.BookResponse
import com.example.library_manager.controller.dto.book.BookUpdateRequest
import com.example.library_manager.controller.dto.book.MessageResponse
import com.example.library_manager.controller.maper.toDomain
import com.example.library_manager.controller.maper.toResponse
import com.example.library_manager.domain.Book
import com.example.library_manager.service.BookService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {

    private val logger = LoggerFactory.getLogger(BookController::class.java)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBook(@Valid @RequestBody request: BookCreateRequest): BookResponse {
        logger.info("Received create book request: $request")
        return bookService.createBook(request.toDomain()).toResponse()
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllBook(pageable: Pageable): Page<Book> {
        return bookService.getAllBooks(pageable)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateBook(@PathVariable id: Long, @Valid @RequestBody request: BookUpdateRequest) {

        bookService.updateBook( request.toDomain(id))
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteBook(@PathVariable id: Long): MessageResponse {
        return bookService.deleteBook(id)
    }

}