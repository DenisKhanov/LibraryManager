package com.example.library_manager.domain.book

import com.example.library_manager.domain.library_item.LibraryItem
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

@Entity
@Table(name = "book")
data class Book(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 255)
    @NotBlank(message = "Title cannot be empty")
    val title: String, //Book nane

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Author cannot be empty")
    val author: String,

    @Column(nullable = false, unique = true, length = 17)
    @NotBlank(message = "ISBN cannot be empty")
    @Pattern(regexp = "^(?:ISBN(?:-13)?:? )?(?=[0-9]{13}$|[0-9]{10}$)(?:97[89][ -]?)?[0-9]{1,5}[ -]?[0-9]+[ -]?[0-9]+[ -]?[0-9]$", message = "Invalid ISBN")
    val isbn: String, //International Standard Book Number

    @Column(name = "published_year")
    @Min(value = 1, message = "Publication year must be positive")
    @Max(value = 9999, message = "Publication year must be valid")
    val publishedYear: Int,

    @OneToMany(mappedBy = "book", cascade = [CascadeType.ALL], orphanRemoval = true)
    val libraryItems: List<LibraryItem> = emptyList()
)