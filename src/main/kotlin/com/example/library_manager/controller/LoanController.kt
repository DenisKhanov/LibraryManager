package com.example.library_manager.controller


import com.example.library_manager.controller.dto.loan.LoanRequest
import com.example.library_manager.controller.dto.loan.LoanResponse
import com.example.library_manager.controller.mapper.toResponse
import com.example.library_manager.service.LoanService
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/loans")
class LoanController(private val loanService: LoanService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createLoan(@Valid @RequestBody request: LoanRequest): LoanResponse {
        val readerId = request.readerId
        val libraryItemId = request.libraryItemId
        return loanService.createLoan(readerId, libraryItemId).toResponse()
    }

    @PostMapping("/{id}/return")
    @ResponseStatus(HttpStatus.CREATED)
    fun returnLoan(@PathVariable("id") id: Long): LoanResponse {
        return loanService.returnLoan(id).toResponse()
    }

    @GetMapping("/readers/{id}/loans")
    fun getLoans(pageable: Pageable, @PathVariable id: Long): Page<LoanResponse> {
        return loanService.getLoans(pageable,id).map { it.toResponse() }
    }
}