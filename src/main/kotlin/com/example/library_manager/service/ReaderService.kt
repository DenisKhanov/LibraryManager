package com.example.library_manager.service

import com.example.library_manager.domain.Reader
import com.example.library_manager.repository.impl.ReaderRepository
import com.example.library_manager.service.exceptions.DuplicateEmailCustomException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service



@Service
class ReaderService(private val readerRepository: ReaderRepository) {
    @Transactional
    fun createReader(reader: Reader): Reader {
        if (readerRepository.existsByEmail(reader.email)) {
            throw DuplicateEmailCustomException(reader.email)
        }
        val newReader = readerRepository.save(reader)
        return newReader
    }

    fun getReaders(pageable: Pageable): Page<Reader> {
        return readerRepository.findAll(pageable)
    }
}