package com.example.library_manager.repository.jpa.entity.library_item

import com.example.library_manager.repository.jpa.entity.book.BookEntity
import com.example.library_manager.repository.jpa.entity.loan.Loan
import jakarta.persistence.*
import jakarta.validation.constraints.Min

@Entity
@Table(name = "library_item")
data class LibraryItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    val bookEntity: BookEntity,

    @Column(name = "total_copies", nullable = false)
    @Min(value = 0, message = "Total copies cannot be negative")
    val totalCopies: Int,

    @Column(name = "available_copies", nullable = false)
    @Min(value = 0, message = "Available copies cannot be negative")
    val availableCopies: Int,

    @OneToMany(mappedBy = "libraryItem", cascade = [CascadeType.ALL], orphanRemoval = true)
    val loans: List<Loan> = emptyList()
)