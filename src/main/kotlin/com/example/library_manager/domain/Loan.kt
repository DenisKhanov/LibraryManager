package com.example.library_manager.domain

import java.time.LocalDateTime

data class Loan(
    val id: Long? = null,
    val reader: Reader,
    val libraryItem: LibraryItem,
    val loanDate: LocalDateTime = LocalDateTime.now(),
    var returnDate: LocalDateTime? = null
)
