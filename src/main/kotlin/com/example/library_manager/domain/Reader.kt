package com.example.library_manager.domain

import com.example.library_manager.repository.jpa.entity.loan.LoanEntity
import java.time.LocalDateTime

data class Reader(
    val id: Long? = null,
    val name: String,
    val email: String,
    val registeredAt: LocalDateTime = LocalDateTime.now(),
    val loanEntities: List<LoanEntity> = emptyList()
)
