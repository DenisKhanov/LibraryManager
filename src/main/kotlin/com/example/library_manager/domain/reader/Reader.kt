package com.example.library_manager.domain.reader

import com.example.library_manager.domain.loan.Loan
import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import jakarta.persistence.Table
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

@Entity
@Table(name = "reader")
data class Reader(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 100)
    @NotBlank(message = "Name cannot be empty")
    val name: String,

    @Column(nullable = false, length = 255)
    @Email(message = "Email must be valid")
    @NotBlank(message = "Email cannot be empty")
    val email: String,

    @Column(name = "registered_at", nullable = false)
    val registeredAt: LocalDateTime = LocalDateTime.now(),

    @OneToMany(mappedBy = "reader", cascade = [CascadeType.ALL], orphanRemoval = true)
    val loans: List<Loan> = emptyList()
)