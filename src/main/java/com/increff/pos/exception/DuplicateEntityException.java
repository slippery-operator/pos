package com.increff.pos.exception;

public class DuplicateEntityException extends ApiException {
    public DuplicateEntityException(String message) {
        super(message);
    }
}
