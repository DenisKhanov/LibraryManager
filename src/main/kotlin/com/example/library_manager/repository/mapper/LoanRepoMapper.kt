package com.example.library_manager.repository.mapper

import com.example.library_manager.domain.Loan
import com.example.library_manager.repository.jpa.entity.loan.LoanEntity

fun Loan.toEntity(): LoanEntity {
    return LoanEntity(
        id = id,
        readerEntity = reader.toEntity(),
        libraryItemEntity = libraryItem.toEntity(),
        loanDate = loanDate,
        returnDate = returnDate,
    )
}

fun LoanEntity.toDomain(): Loan {
    return Loan(
        id = id,
        reader = readerEntity.toDomain(),
        libraryItem = libraryItemEntity!!.toDomain(),
        loanDate = loanDate,
        returnDate = returnDate,
    )
}