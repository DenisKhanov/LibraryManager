package com.example.library_manager.domain

import com.example.library_manager.repository.jpa.entity.library_item.LibraryItemEntity

data class Book(
    val id: Long? = null,

    val title: String,

    val author: String,
    /**
     * International Standard Book Number
     */
    val isbn: String,

    val publishedYear: Int,

    val libraryItemEntities: List<LibraryItemEntity> = emptyList()
)