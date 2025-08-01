package com.example.library_manager.domain


data class LibraryItem(
    val id: Long? = null,

    val book: Book,

    val totalCopies: Int,

    var availableCopies: Int,

    )
