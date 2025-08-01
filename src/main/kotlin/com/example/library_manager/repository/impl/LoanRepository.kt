package com.example.library_manager.repository.impl

import com.example.library_manager.domain.Loan
import com.example.library_manager.repository.jpa.LoanJpaRepository
import com.example.library_manager.repository.mapper.toDomain
import com.example.library_manager.repository.mapper.toEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class LoanRepository(private val loanJpaRepository: LoanJpaRepository) {
    fun save(loan: Loan): Loan {
        val loanEntity = loan.toEntity()
        val savedLoan = loanJpaRepository.save(loanEntity)
        return savedLoan.toDomain()
    }

    fun findById(loanId: Long): Loan? {
        val loanEntity = loanJpaRepository.findByIdOrNull(loanId)
        return loanEntity?.toDomain()
    }

    fun findAll(pageable: Pageable): Page<Loan> {
        return loanJpaRepository.findAll(pageable).map { it.toDomain() }
    }
}