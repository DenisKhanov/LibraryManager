package com.example.library_manager.dto.library_item

data class LibraryItemResponse(
    val id: Long,
    val bookId: Long,
    val totalCopies: Int,
    val availableCopies: Int
)