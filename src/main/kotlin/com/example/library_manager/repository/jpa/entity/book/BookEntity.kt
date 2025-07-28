package com.example.library_manager.repository.jpa.entity.book

import com.example.library_manager.repository.jpa.entity.library_item.LibraryItemEntity
import jakarta.persistence.*

@Entity
@Table(name = "book")
data class BookEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 255)
    val title: String,

    @Column(nullable = false, length = 100)
    val author: String,

    @Column(nullable = false, unique = true, length = 17)
    val isbn: String,

    @Column(name = "published_year")
    val publishedYear: Int,

    @OneToMany(mappedBy = "bookEntity", cascade = [CascadeType.PERSIST, CascadeType.MERGE], orphanRemoval = false)
    val libraryItemEntities: Set<LibraryItemEntity> = emptySet()
)