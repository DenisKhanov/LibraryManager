package com.example.library_manager.repository.mapper

import com.example.library_manager.domain.LibraryItem
import com.example.library_manager.repository.jpa.entity.library_item.LibraryItemEntity

fun LibraryItem.toEntity(): LibraryItemEntity {
    return LibraryItemEntity(
        id = id,
        bookEntity = book.toEntity(),
        totalCopies = totalCopies,
        availableCopies = availableCopies,
    )
}

fun LibraryItemEntity.toDomain(): LibraryItem{
    return LibraryItem(
        id = id!!, //TODO{добавить проверку или изменить entity}
        book = bookEntity.toDomain(),
        totalCopies = totalCopies,
        availableCopies = availableCopies,
    )
}