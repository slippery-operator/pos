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
//    TODO: create package called constants in model, define enums there
    public enum ErrorType {

        VALIDATION_ERROR(400, "Validation Error"),

        FILE_SIZE_EXCEEDED(413, "File Size Exceeded"),
        BAD_REQUEST(400, "Bad Request"),
        NOT_FOUND(404, "Not Found"),
        CONFLICT(409, "Conflict"),
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
