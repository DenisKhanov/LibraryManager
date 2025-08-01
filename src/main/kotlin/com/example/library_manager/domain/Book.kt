package com.example.library_manager.domain


data class Book(
    val id: Long? = null,

    val title: String,

    val author: String,
    /**
     * International Standard Book Number
     */
    val isbn: String,

    val publishedYear: Int,
)