package com.example.library_manager.controller

import com.example.library_manager.dto.book.BookRequest
import com.example.library_manager.dto.book.BookResponse
import com.example.library_manager.dto.book.MessageResponse
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
    fun createBook(@Valid @RequestBody request: BookRequest): BookResponse {
        return bookService.createBook(request)
    }


    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    fun getAllBook(pageable: Pageable): Page<BookResponse> {
        return bookService.getAllBooks(pageable)
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun updateBook(@PathVariable id: Long, @Valid @RequestBody request: BookRequest) {
        bookService.updateBook(id, request)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    fun deleteBook(@PathVariable id: Long): MessageResponse {
        return bookService.deleteBook(id)
    }

}