package com.example.library_manager.repository.jpa.entity.reader

import com.example.library_manager.repository.jpa.entity.loan.Loan
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reader")
data class Reader(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    val name: String,

    @Column(nullable = false, length = 255)
    val email: String,

    @Column(name = "registered_at", nullable = false)
    val registeredAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "reader", cascade = [CascadeType.ALL], orphanRemoval = true)
    val loans: List<Loan> = emptyList()
)