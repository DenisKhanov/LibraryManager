package com.example.library_manager.domain

import java.time.LocalDateTime

data class Reader(
    val id: Long? = null,
    val name: String,
    val email: String,
    val registeredAt: LocalDateTime = LocalDateTime.now(),
)
