package com.increff.invoice.exception;

/**
 * Custom exception class for API errors
 * Provides different error types for better error handling
 */
public class ApiException extends RuntimeException {

    private ErrorType type;

    public ApiException(ErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public ApiException(ErrorType type, String message, Throwable cause) {
        super(message, cause);
        this.type = type;
    }

    public ErrorType getType() {
        return type;
    }

    /**
     * Enum defining different types of API errors
     */
    public enum ErrorType {
        VALIDATION_ERROR,
        ENTITY_NOT_FOUND,
        DUPLICATE_ENTRY,
        INTERNAL_ERROR,
        EXTERNAL_SERVICE_ERROR
    }
} 