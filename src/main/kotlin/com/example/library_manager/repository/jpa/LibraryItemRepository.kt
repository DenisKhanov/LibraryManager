package com.example.library_manager.repository.jpa

import com.example.library_manager.repository.jpa.entity.library_item.LibraryItem
import org.springframework.data.jpa.repository.JpaRepository

interface LibraryItemRepository : JpaRepository<LibraryItem, Long>