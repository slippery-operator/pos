package com.increff.pos.model.response;

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
    private String timestamp;
    private String path;

    public ErrorResponse(String error, String message, String path) {
        this.error = error;
        this.message = message;
        this.path = path;
        this.timestamp = DateUtil.getCurrentFormattedTimestamp();
    }
}
