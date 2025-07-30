package com.example.library_manager.controller.dto.loan

import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class LoanRequest(
    @field:NotNull(message = "Reader ID cannot be null")
    @field:Positive(message = "Reader ID must be a positive number")
    val readerId: Long,

    @field:NotNull(message = "Library item ID cannot be null")
    @field:Positive(message = "Library item ID must be a positive number")
    val libraryItemId: Long
)
