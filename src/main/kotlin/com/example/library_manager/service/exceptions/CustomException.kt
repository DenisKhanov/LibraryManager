package com.example.library_manager.service.exceptions

open class CustomException(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class DuplicateIsbnCustomException(isbn: String) : CustomException("Book with ISBN $isbn already exists")

class ResourceNotFoundCustomException(resource: String, id: Long) : CustomException("$resource with ID $id not found")

class DuplicateEmailCustomException(email: String) : CustomException("Reader with email $email already exists")

class LoanAlreadyReturnedCustomException(loanId: Long) :
    CustomException("Loan with ID $loanId has already been returned")

class NotAvailableCustomException(resource: String,id: Long) : CustomException("$resource with ID $id is not available")

class IllegalArgumentCustomException(resource: Int) : CustomException("Total copies must be greater than $resource")

