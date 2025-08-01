package com.example.library_manager.service

import com.example.library_manager.domain.Loan
import com.example.library_manager.repository.impl.LibraryItemRepository
import com.example.library_manager.repository.impl.LoanRepository
import com.example.library_manager.repository.impl.ReaderRepository
import com.example.library_manager.service.exceptions.LoanAlreadyReturnedCustomException
import com.example.library_manager.service.exceptions.NotAvailableCustomException
import com.example.library_manager.service.exceptions.ResourceNotFoundCustomException
import jakarta.transaction.Transactional
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class LoanService(
    private val loanRepository: LoanRepository,
    private val readerRepository: ReaderRepository,
    private val libraryItemRepository: LibraryItemRepository,
) {
    @Transactional
    fun createLoan(readerId: Long, libraryItemId: Long): Loan {
        val reader = readerRepository.findById(readerId) ?: throw ResourceNotFoundCustomException("Reader", readerId)

        val libraryItem = libraryItemRepository.findById(libraryItemId) ?: throw ResourceNotFoundCustomException(
            "Library item",
            libraryItemId
        )


        if (libraryItem.availableCopies <= 0) {
            throw NotAvailableCustomException("Library item", libraryItemId)
        }
        libraryItem.availableCopies -= 1
        libraryItemRepository.save(libraryItem)

        val loan = Loan(
            reader = reader,
            libraryItem = libraryItem,
            loanDate = LocalDateTime.now(),
        )
        val savedLoan = loanRepository.save(loan)
        return savedLoan
    }

    @Transactional
    fun returnLoan(loanId: Long): Loan {

        val loan = loanRepository.findById(loanId) ?: throw ResourceNotFoundCustomException("Loan", loanId)

        if (loan.returnDate != null) {
            throw LoanAlreadyReturnedCustomException(loanId)
        }

        val libraryItemId = loan.libraryItem.id

        val libraryItem = libraryItemRepository.findById(libraryItemId!!) ?: throw ResourceNotFoundCustomException(
            "Library item",
            libraryItemId
        )

        libraryItem.availableCopies += + 1
        libraryItemRepository.save(libraryItem)

        loan.returnDate = LocalDateTime.now()
        val savedLoan = loanRepository.save(loan)
        return savedLoan
    }

    fun getLoans(pageable: Pageable, readerId: Long): Page<Loan> {
        if (!readerRepository.existsById(readerId)) {
            throw ResourceNotFoundCustomException("Reader", readerId)
        }
        return loanRepository.findAll(pageable)

    }
}