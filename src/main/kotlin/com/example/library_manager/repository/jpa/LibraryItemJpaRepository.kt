package com.example.library_manager.repository.jpa

import com.example.library_manager.repository.jpa.entity.library_item.LibraryItemEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository

interface LibraryItemJpaRepository : JpaRepository<LibraryItemEntity, Long>{
    @EntityGraph("LibraryItemEntity.withBook")
    override fun findAll(pageable: Pageable): Page<LibraryItemEntity>
}