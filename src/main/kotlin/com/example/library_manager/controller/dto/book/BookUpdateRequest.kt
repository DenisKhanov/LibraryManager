package com.example.library_manager.controller.dto.book

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern

data class BookUpdateRequest(
    val id: Long,

    val title: String,

    val author: String,

    @field:Pattern(
        regexp = "^(?:ISBN(?:-13)?:? )?(?=[0-9]{13}$|[0-9]{10}$)(?:97[89][ -]?)?[0-9]{1,5}[ -]?[0-9]+[ -]?[0-9]+[ -]?[0-9]$",
        message = "Invalid ISBN"
    )
    val isbn: String,

    @field:Min(value = 1, message = "Publication year must be positive")
    @field:Max(value = 9999, message = "Publication year must be valid")
    val publishedYear: Int
)
