package com.example.library_manager.repository

import com.example.library_manager.domain.book.Book
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface BookRepository : JpaRepository<Book, Long>{
    fun existsByIsbn(isbn: String): Boolean
}





