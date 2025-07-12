package com.increff.pos.exception;
import com.increff.pos.model.enums.ErrorType;

public class ApiException extends RuntimeException {
    private final ErrorType errorType;

    public ApiException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }


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
