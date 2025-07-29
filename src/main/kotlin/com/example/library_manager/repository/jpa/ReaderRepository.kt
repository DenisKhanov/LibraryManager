package com.example.library_manager.repository.jpa

import com.example.library_manager.repository.jpa.entity.reader.Reader
import org.springframework.data.jpa.repository.JpaRepository

interface ReaderRepository : JpaRepository<Reader, Long>