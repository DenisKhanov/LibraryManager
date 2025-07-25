package com.example.library_manager.repository.jpa.entity.loan

import com.example.library_manager.repository.jpa.entity.library_item.LibraryItem
import com.example.library_manager.repository.jpa.entity.reader.Reader
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "loan")
data class Loan(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reader_id", nullable = false)
    val reader: Reader,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "library_item_id", nullable = false)
    val libraryItem: LibraryItem,

    @Column(name = "loan_date", nullable = false)
    val loanDate: LocalDateTime = LocalDateTime.now(),

    @Column(name = "return_date")
    val returnDate: LocalDateTime? = null
)