package com.example.library_manager.controller.mapper

import com.example.library_manager.controller.dto.loan.LoanResponse
import com.example.library_manager.domain.Loan

fun Loan.toResponse(): LoanResponse {
    return LoanResponse(
        id = id!!,
        readerId = reader.id!!,
        libraryItemId = libraryItem.id!!,
        loanDate = loanDate,
        returnDate = returnDate,
    )
}