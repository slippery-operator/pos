package com.increff.pos.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
public class ErrorResponse {
    private String error;
    private String message;
    private ZonedDateTime timestamp;
    private String path;

    public ErrorResponse(String error, String message, String path) {
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = ZonedDateTime.now();
    }
}
