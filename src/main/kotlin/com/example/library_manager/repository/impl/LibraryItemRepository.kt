package com.example.library_manager.repository.impl

import com.example.library_manager.domain.LibraryItem
import com.example.library_manager.repository.jpa.LibraryItemJpaRepository
import com.example.library_manager.repository.mapper.toDomain
import com.example.library_manager.repository.mapper.toEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class LibraryItemRepository(val libraryItemJpaRepository: LibraryItemJpaRepository) {


    fun save(libraryItem: LibraryItem): LibraryItem {
        val itemEntity = libraryItem.toEntity()
        val savedEntity = libraryItemJpaRepository.save(itemEntity)
        return savedEntity.toDomain()
    }

    fun findById(id: Long): LibraryItem? {
        val libraryItem = libraryItemJpaRepository.findByIdOrNull(id)
        return libraryItem?.toDomain()
    }

    fun findAll(pageable: Pageable): Page<LibraryItem> {
        return libraryItemJpaRepository.findAll(pageable).map { it.toDomain() }
    }


}