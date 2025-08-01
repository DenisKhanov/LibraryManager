package com.example.library_manager.repository.mapper

import com.example.library_manager.domain.Reader
import com.example.library_manager.repository.jpa.entity.reader.ReaderEntity

fun Reader.toEntity(): ReaderEntity {
    return ReaderEntity(
        id = id,
        name = name,
        email = email,
        registeredAt = registeredAt,
    )
}

fun ReaderEntity.toDomain(): Reader {
    return Reader(
        id = id,
        name = name,
        email = email,
        registeredAt = registeredAt,
    )
}