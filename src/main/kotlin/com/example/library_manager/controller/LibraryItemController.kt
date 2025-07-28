package com.example.library_manager.controller


import com.example.library_manager.controller.dto.library_item.LibraryItemRequest
import com.example.library_manager.controller.dto.library_item.LibraryItemResponse
import com.example.library_manager.controller.mapper.toResponse
import com.example.library_manager.service.LibraryItemService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/library-items")
class LibraryItemController(private val libraryItemService: LibraryItemService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createLibraryItem(@Valid @RequestBody request: LibraryItemRequest): LibraryItemResponse {
        return libraryItemService.createLibraryItem(request.bookId,request.totalCopies).toResponse()
    }

    @GetMapping
    fun getAllLibraryItems(pageable: Pageable): Page<LibraryItemResponse> {
        return libraryItemService.getAllLibraryItems(pageable).map { it.toResponse() }
    }
}