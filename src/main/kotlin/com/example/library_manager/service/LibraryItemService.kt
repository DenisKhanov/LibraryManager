package com.example.library_manager.service

import com.example.library_manager.repository.jpa.entity.library_item.LibraryItem
import com.example.library_manager.controller.dto.book.BookResponse
import com.example.library_manager.controller.dto.library_item.LibraryItemRequest
import com.example.library_manager.controller.dto.library_item.LibraryItemResponse
import com.example.library_manager.repository.jpa.BookJpaRepository
import com.example.library_manager.repository.jpa.LibraryItemRepository
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class LibraryItemService(
    private val libraryItemRepository: LibraryItemRepository,
    private val bookJpaRepository: BookJpaRepository
) {

    @Transactional
    fun createLibraryItem(request: LibraryItemRequest): LibraryItemResponse {
        val book = bookJpaRepository.findById(request.bookId)
            .orElseThrow { ResponseStatusException(HttpStatus.NOT_FOUND, "Book with id ${request.bookId} not found") }

        val libraryItem = LibraryItem(
            bookEntity = book,
            totalCopies = request.totalCopies,
            availableCopies = request.totalCopies // При создании все копии доступны
        )

        val savedLibraryItem = libraryItemRepository.save(libraryItem)

        return LibraryItemResponse(
            id = savedLibraryItem.id!!,
            book = BookResponse(
                id = book.id!!,
                title = book.title,
                author = book.author,
                isbn = book.isbn,
                publishedYear = book.publishedYear
            ),
            totalCopies = savedLibraryItem.totalCopies,
            availableCopies = savedLibraryItem.availableCopies
        )
    }


    fun getAllLibraryItems(pageable: Pageable): Page<LibraryItemResponse> {
        return libraryItemRepository.findAll(pageable).map { libraryItem ->
            LibraryItemResponse(
                id = libraryItem.id!!,
                book = BookResponse(
                    id = libraryItem.bookEntity.id!!,
                    title = libraryItem.bookEntity.title,
                    author = libraryItem.bookEntity.author,
                    isbn = libraryItem.bookEntity.isbn,
                    publishedYear = libraryItem.bookEntity.publishedYear
                ),
                totalCopies = libraryItem.totalCopies,
                availableCopies = libraryItem.availableCopies
            )
        }
    }
}