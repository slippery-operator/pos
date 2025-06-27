package com.increff.pos.exception;

public class EntityNotFoundException extends ApiException {
    public EntityNotFoundException(String message) {
        super(message);
    }
}
