package com.example.library_manager.controller.dto.library_item

import com.example.library_manager.controller.dto.book.BookResponse

data class LibraryItemResponse(
    val id: Long,
    val book: BookResponse,
    val totalCopies: Int,
    val availableCopies: Int
)