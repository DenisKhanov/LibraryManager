package com.example.library_manager.domain

import com.example.library_manager.repository.jpa.entity.library_item.LibraryItem

data class Book(
    val id: Long? = null,

    val title: String, //Book nane

    val author: String,

    val isbn: String, //International Standard Book Number

    val publishedYear: Int,

    val libraryItems: List<LibraryItem> = emptyList()
)