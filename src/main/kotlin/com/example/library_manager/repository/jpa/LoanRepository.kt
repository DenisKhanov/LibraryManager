package com.example.library_manager.repository.jpa

import com.example.library_manager.repository.jpa.entity.loan.Loan
import org.springframework.data.jpa.repository.JpaRepository

interface LoanRepository : JpaRepository<Loan, Long>