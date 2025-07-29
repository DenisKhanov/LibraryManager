package com.example.library_manager.controller.mapper

import com.example.library_manager.controller.dto.library_item.LibraryItemResponse
import com.example.library_manager.domain.LibraryItem

fun LibraryItem.toResponse(): LibraryItemResponse {
    val itemId = id
    return LibraryItemResponse(
        id = itemId!!,
        book = book.toResponse(),
        totalCopies = totalCopies,
        availableCopies = availableCopies,
    )
}
