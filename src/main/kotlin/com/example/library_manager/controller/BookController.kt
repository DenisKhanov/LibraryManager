package com.example.library_manager.controller

import com.example.library_manager.controller.dto.book.BookCreateRequest
import com.example.library_manager.controller.dto.book.BookResponse
import com.example.library_manager.controller.dto.book.BookUpdateRequest
import com.example.library_manager.controller.mapper.toDomain
import com.example.library_manager.controller.mapper.toResponse
import com.example.library_manager.service.BookService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/books")
class BookController(private val bookService: BookService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createBook(@Valid @RequestBody request: BookCreateRequest): BookResponse {
        return bookService.createBook(request.toDomain()).toResponse()
    }

    @GetMapping
    fun getAllBook(pageable: Pageable): Page<BookResponse> {
        return bookService.getAllBooks(pageable).map { it.toResponse() }
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun updateBook(@PathVariable id: Long, @Valid @RequestBody request: BookUpdateRequest) {
        bookService.updateBook( request.toDomain(id))
    }

    @DeleteMapping("/{id}")
    fun deleteBook(@PathVariable id: Long) {
        bookService.deleteBook(id)
    }
}