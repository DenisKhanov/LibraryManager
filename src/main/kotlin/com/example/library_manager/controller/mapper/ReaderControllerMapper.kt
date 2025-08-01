package com.example.library_manager.controller.mapper

import com.example.library_manager.controller.dto.reader.ReaderRequest
import com.example.library_manager.controller.dto.reader.ReaderResponse
import com.example.library_manager.domain.Reader

fun ReaderRequest.toDomain(): Reader {
    return Reader(
        name = name,
        email = email,
    )
}
fun Reader.toResponse(): ReaderResponse {
    return ReaderResponse(
        id=id!!,
        name=name,
        email=email,
        registeredAt=registeredAt,
    )
}