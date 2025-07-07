package com.increff.pos.exception;

/**
 * ApiException is the main exception class for the application.
 * It uses an ErrorType enum to categorize different types of errors,
 * which helps reduce redundant code in exception handlers.
 * 
 * The enum approach allows us to:
 * 1. Have a single exception class instead of multiple specific ones
 * 2. Categorize errors with proper HTTP status codes
 * 3. Reduce boilerplate code in RestControllerAdvice
 * 4. Make error handling more maintainable and consistent
 */
public class ApiException extends RuntimeException {
    
    /**
     * Enum to categorize different types of errors.
     * Each enum value maps to a specific HTTP status code and error type.
     * This eliminates the need for multiple exception classes.
     */
    public enum ErrorType {
//        VALIDATION_ERROR(400, "VALIDATION_ERROR"),
//        ENTITY_NOT_FOUND(404, "ENTITY_NOT_FOUND"),
//        RESOURCE_ALREADY_EXISTS(409, "RESOURCE_ALREADY_EXISTS"),
//        DUPLICATE_ENTRY(409, "DUPLICATE_ENTRY"),
//        FILE_SIZE_EXCEEDED(400, "FILE_SIZE_EXCEEDED"),
//        INVALID_FORM(400, "INVALID_FORM"),
//        EXTERNAL_SERVICE_ERROR(502, "EXTERNAL_SERVICE_ERROR"),
//        INTERNAL_ERROR(500, "INTERNAL_ERROR"),
//        INTERNAL_SERVER_ERROR(500, "INTERNAL_SERVER_ERROR");

        VALIDATION_ERROR(400, "Validation Error"),
        ENTITY_NOT_FOUND(404, "Entity Not Found"),
        DUPLICATE_ENTRY(409, "Duplicate Entry"),
        FILE_SIZE_EXCEEDED(413, "File Size Exceeded"),
        INVALID_FORM(400, "Invalid Form"),
        EXTERNAL_SERVICE_ERROR(502, "External Service Error"),
        INTERNAL_ERROR(500, "Internal Error"),
        BAD_REQUEST(400, "Bad Request"),
        NOT_FOUND(404, "Not Found"),
        CONFLICT(409, "Conflict"),
        PAYLOAD_TOO_LARGE(413, "Payload Too Large"),
        BAD_GATEWAY(502, "Bad Gateway"),
        INTERNAL_SERVER_ERROR(500, "Internal Server Error");


        private final int httpStatus;
        private final String errorCode;
        
        ErrorType(int httpStatus, String errorCode) {
            this.httpStatus = httpStatus;
            this.errorCode = errorCode;
        }
        
        public int getHttpStatus() {
            return httpStatus;
        }
        
        public String getErrorCode() {
            return errorCode;
        }
    }
    
    private final ErrorType errorType;
    
    /**
     * Constructor with error type and message
     * @param errorType The type of error (enum)
     * @param message The error message
     */
    public ApiException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }
    
    /**
     * Constructor with error type, message, and cause
     * @param errorType The type of error (enum)
     * @param message The error message
     * @param cause The underlying cause of the exception
     */
    public ApiException(ErrorType errorType, String message, Throwable cause) {
        super(message, cause);
        this.errorType = errorType;
    }
    
    /**
     * Get the error type enum
     * @return The ErrorType enum value
     */
    public ErrorType getErrorType() {
        return errorType;
    }
    
    /**
     * Get the HTTP status code for this error type
     * @return HTTP status code
     */
    public int getHttpStatus() {
        return errorType.getHttpStatus();
    }
    
    /**
     * Get the error code string for this error type
     * @return Error code string
     */
    public String getErrorCode() {
        return errorType.getErrorCode();
    }
}
