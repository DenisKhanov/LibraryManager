package com.example.library_manager.controller.dto.loan

import java.time.LocalDateTime

data class LoanResponse(
    val id: Long,
    val readerId: Long,
    val libraryItemId: Long,
    val loanDate: LocalDateTime,
    val returnDate: LocalDateTime?
)