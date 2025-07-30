package com.example.library_manager.repository.impl

import com.example.library_manager.domain.Reader
import com.example.library_manager.repository.jpa.ReaderJpaRepository
import com.example.library_manager.repository.mapper.toDomain
import com.example.library_manager.repository.mapper.toEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository


@Repository
class ReaderRepository (val readerJpaRepository: ReaderJpaRepository){
    fun save(reader: Reader): Reader {
        val readerEntity = reader.toEntity()
        val savedEntity = readerJpaRepository.save(readerEntity)
        return savedEntity.toDomain()
    }

    fun findAll(pageable: Pageable): Page<Reader> {
        return readerJpaRepository.findAll(pageable).map { it.toDomain() }
    }

    fun findById(id: Long): Reader? {
        val readerEntity = readerJpaRepository.findByIdOrNull(id)
        return readerEntity?.toDomain()
    }

    fun existsByEmail(email: String): Boolean {
        return readerJpaRepository.existsByEmail(email)
    }
    fun existsById(readerId: Long): Boolean {
        return readerJpaRepository.existsById(readerId)
    }
}