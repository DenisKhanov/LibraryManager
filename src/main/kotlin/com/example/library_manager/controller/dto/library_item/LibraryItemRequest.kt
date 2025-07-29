package com.example.library_manager.controller.dto.library_item

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class LibraryItemRequest(
    @field:NotNull(message = "Book ID cannot be null")
    val bookId: Long,

    @field:NotNull(message = "Total copies cannot be null")
    @field:Min(value = 1, message = "Total copies must be positive")
    val totalCopies: Int
)