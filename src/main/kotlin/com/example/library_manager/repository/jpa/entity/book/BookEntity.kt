package com.example.library_manager.repository.jpa.entity.book

import com.example.library_manager.repository.jpa.entity.library_item.LibraryItem
import jakarta.persistence.*

@Entity
@Table(name = "book")
data class BookEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 255)
    val title: String, //Book nane

    @Column(nullable = false, length = 100)
    val author: String,

    @Column(nullable = false, unique = true, length = 17)
    val isbn: String, //International Standard Book Number

    @Column(name = "published_year")
    val publishedYear: Int,

    @OneToMany(mappedBy = "bookEntity", cascade = [CascadeType.ALL], orphanRemoval = true)
    val libraryItems: List<LibraryItem> = emptyList()
)