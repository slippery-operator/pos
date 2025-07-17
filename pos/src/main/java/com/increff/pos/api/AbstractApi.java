package com.increff.pos.api;

import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;

public abstract class AbstractApi {
    protected <T> T checkNotNull(T object, String message) {
        if (object == null) {
            throw new ApiException(ErrorType.NOT_FOUND, message);
        }
        return object;
    }
    protected void checkConflict(boolean condition, String message) {
        if(condition) {
            throw new ApiException(ErrorType.CONFLICT, message);
        }
    }
    protected void checkBadRequest(boolean condition, String message) {
        if(condition) {
            throw new ApiException(ErrorType.BAD_REQUEST, message);
        }
    }
}
