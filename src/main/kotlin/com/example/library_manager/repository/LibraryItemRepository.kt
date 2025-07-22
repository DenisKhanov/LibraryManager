package com.example.library_manager.repository

import com.example.library_manager.domain.library_item.LibraryItem
import org.springframework.data.jpa.repository.JpaRepository

interface LibraryItemRepository : JpaRepository<LibraryItem, Long>