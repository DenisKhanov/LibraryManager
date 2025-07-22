package com.example.library_manager.dto.reader

import java.time.LocalDateTime

data class ReaderResponse(
    val id: Long,
    val name: String,
    val email: String,
    val registeredAt: LocalDateTime
)