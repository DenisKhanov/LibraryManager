package com.example.library_manager.service

import com.example.library_manager.domain.LibraryItem
import com.example.library_manager.repository.impl.BookRepository
import com.example.library_manager.repository.impl.LibraryItemRepository
import com.example.library_manager.service.exceptions.ResourceNotFoundException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service

@Service
class LibraryItemService(
    private val libraryItemRepository: LibraryItemRepository,
    private val bookRepository: BookRepository
) {

    @Transactional
    fun createLibraryItem(bookId: Long, totalCopies: Int): LibraryItem {
        val book = bookRepository.findById(bookId) ?: throw ResourceNotFoundException("Book",bookId)

        val libraryItem = LibraryItem(
            book = book,
            totalCopies = totalCopies,
            availableCopies = totalCopies,
        )
        val savedLibraryItem = libraryItemRepository.save(libraryItem)

        return savedLibraryItem
    }


    fun getAllLibraryItems(pageable: Pageable): Page<LibraryItem> {
        return libraryItemRepository.findAll(pageable)
    }
}