package com.increff.invoice.exception;

public class ApiException extends RuntimeException {

    private ErrorType type;

    public ApiException(ErrorType type, String message) {
        super(message);
        this.type = type;
    }

    public enum ErrorType {
        INTERNAL_SERVER_ERROR
    }
} 