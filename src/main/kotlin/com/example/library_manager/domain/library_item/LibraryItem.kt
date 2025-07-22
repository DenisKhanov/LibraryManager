package com.example.library_manager.domain.library_item

import com.example.library_manager.domain.book.Book
import com.example.library_manager.domain.loan.Loan
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.Min

@Entity
@Table(name = "library_item")
data class LibraryItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    val book: Book,

    @Column(name = "total_copies", nullable = false)
    @Min(value = 0, message = "Total copies cannot be negative")
    val totalCopies: Int,

    @Column(name = "available_copies", nullable = false)
    @Min(value = 0, message = "Available copies cannot be negative")
    val availableCopies: Int,

    @OneToMany(mappedBy = "libraryItem", cascade = [CascadeType.ALL], orphanRemoval = true)
    val loans: List<Loan> = emptyList()
)