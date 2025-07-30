package com.example.library_manager.repository.jpa

import com.example.library_manager.repository.jpa.entity.loan.LoanEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface LoanJpaRepository : JpaRepository<LoanEntity, Long>{
    @EntityGraph("LoanEntity.withLibraryItem")
    override fun findAll(pageable: Pageable): Page<LoanEntity>

}