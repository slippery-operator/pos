package com.increff.pos.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.increff.pos.util.DateUtil;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class ErrorResponse {
    private String error;
    private String message;
    @JsonFormat(pattern = "dd MMM yyyy, h:mm a z", timezone = "UTC")
    private ZonedDateTime timestamp;
    private String path;

    public ErrorResponse(String error, String message, String path) {
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = ZonedDateTime.now();
    }
}
