package com.example.library_manager.repository.jpa

import com.example.library_manager.repository.jpa.entity.book.BookEntity
import org.springframework.data.jpa.repository.JpaRepository

interface BookJpaRepository : JpaRepository<BookEntity, Long>{
    fun existsByIsbn(isbn: String): Boolean
}





