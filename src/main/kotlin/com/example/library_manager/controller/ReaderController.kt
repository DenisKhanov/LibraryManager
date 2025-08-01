package com.example.library_manager.controller

import com.example.library_manager.controller.dto.reader.ReaderRequest
import com.example.library_manager.controller.dto.reader.ReaderResponse
import com.example.library_manager.controller.mapper.toDomain
import com.example.library_manager.controller.mapper.toResponse
import com.example.library_manager.service.ReaderService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/readers")
class ReaderController(private val readerService: ReaderService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createReader(@Valid @RequestBody request: ReaderRequest): ReaderResponse {
        return readerService.createReader(request.toDomain()).toResponse()
    }

    @GetMapping
    fun getReaders(pageable: Pageable): Page<ReaderResponse> {
        return readerService.getReaders(pageable).map{it.toResponse()}
    }
}