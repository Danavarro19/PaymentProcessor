package com.org.paymentprocessor.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.OffsetDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicatePaymentException.class)
    public ResponseEntity<ApiError> handleDuplicatePayment(
            DuplicatePaymentException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "Duplicate payment",
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(error);
    }

    @ExceptionHandler(PaymentProcessingException.class)
    public ResponseEntity<ApiError> handlePaymentProcessing(
            PaymentProcessingException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Payment processing failed",
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(error);
    }

    @ExceptionHandler(InvalidDateRangeException.class)
    public ResponseEntity<ApiError> handleInvalidDateRange(
            InvalidDateRangeException exception,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Invalid date range",
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField() + ": "
                                + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        ApiError error = new ApiError(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                message,
                request.getRequestURI()
        );

        return ResponseEntity
                .badRequest()
                .body(error);
    }
}