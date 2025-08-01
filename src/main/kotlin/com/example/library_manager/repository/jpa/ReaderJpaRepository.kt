package com.example.library_manager.repository.jpa

import com.example.library_manager.repository.jpa.entity.reader.ReaderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface ReaderJpaRepository : JpaRepository<ReaderEntity, Long>{
    fun existsByEmail(email: String): Boolean
}