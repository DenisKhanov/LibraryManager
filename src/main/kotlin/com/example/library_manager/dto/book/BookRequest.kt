package com.example.library_manager.dto.book

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class BookRequest(
    @NotBlank(message = "Title cannot be empty")
    val title: String,

    @NotBlank(message = "Author cannot be empty")
    val author: String,

    @NotBlank(message = "ISBN cannot be empty")
    @Pattern(
        regexp = "^(?:ISBN(?:-13)?:? )?(?=[0-9]{13}$|[0-9]{10}$)(?:97[89][ -]?)?[0-9]{1,5}[ -]?[0-9]+[ -]?[0-9]+[ -]?[0-9]$",
        message = "Invalid ISBN"
    )
    val isbn: String,

    @Min(value = 1, message = "Publication year must be positive")
    @Max(value = 9999, message = "Publication year must be valid")
    val publishedYear: Int
)