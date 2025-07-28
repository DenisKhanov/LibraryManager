package com.example.library_manager.config

import com.example.library_manager.service.exceptions.DuplicateIsbnException
import com.example.library_manager.service.exceptions.Exception
import com.example.library_manager.service.exceptions.ResourceNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<Map<String, String>> {
        val errors = mutableMapOf<String, String>()
        ex.bindingResult.allErrors.forEach { error ->
            val fieldName = (error as? FieldError)?.field ?: "object"
            val errorMessage = error.defaultMessage ?: "Validation failed"
            errors[fieldName] = errorMessage
        }
        return ResponseEntity(errors, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(DuplicateIsbnException::class)
    fun handleDuplicateIsbnException(ex: DuplicateIsbnException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.BAD_REQUEST.value(),
                error = HttpStatus.BAD_REQUEST.reasonPhrase,
                message = ex.message ?: "Duplicate ISBN"
            ),
            HttpStatus.BAD_REQUEST
        )
    }

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.NOT_FOUND.value(),
                error = HttpStatus.NOT_FOUND.reasonPhrase,
                message = ex.message ?: "Resource not found"
            ),
            HttpStatus.NOT_FOUND
        )
    }


    @ExceptionHandler(Exception::class)
    fun handleLibraryException(ex: Exception): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
            ErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR.value(),
                error = HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase,
                message = ex.message ?: "Unexpected error"
            ),
            HttpStatus.INTERNAL_SERVER_ERROR
        )
    }
}

data class ErrorResponse(
    val status: Int,
    val error: String,
    val message: String,
    val details: Map<String, String>? = null
)