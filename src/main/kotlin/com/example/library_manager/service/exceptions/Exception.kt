package com.example.library_manager.service.exceptions

open class Exception(message: String, cause: Throwable? = null) : RuntimeException(message, cause)

class DuplicateIsbnException(isbn: String) : Exception("Book with ISBN $isbn already exists")

class ResourceNotFoundException(resource: String, id: Long) : Exception("$resource with id $id not found")

class DuplicateEmailException(email: String) : Exception("Reader with email $email already exists")