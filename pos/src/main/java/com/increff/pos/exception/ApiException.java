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
    public ErrorType getErrorType() {
        return errorType;
    }

    public int getHttpStatus() {
        return errorType.getHttpStatus();
    }

    public String getErrorCode() {
        return errorType.getErrorCode();
    }
}
