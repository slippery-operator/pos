package com.increff.pos.model.enums;

public enum ErrorType {

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
