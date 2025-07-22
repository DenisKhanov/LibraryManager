package com.example.library_manager.repository

import com.example.library_manager.domain.loan.Loan
import org.springframework.data.jpa.repository.JpaRepository

interface LoanRepository : JpaRepository<Loan, Long>