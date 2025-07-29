package com.example.library_manager.controller.dto.book

data class BookResponse(
    val id: Long,
    val title: String,
    val author: String,
    val isbn: String,
    val publishedYear: Int
)