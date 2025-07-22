package com.example.library_manager.repository

import com.example.library_manager.domain.reader.Reader
import org.springframework.data.jpa.repository.JpaRepository

interface ReaderRepository : JpaRepository<Reader, Long>