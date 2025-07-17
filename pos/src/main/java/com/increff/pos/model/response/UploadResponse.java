package com.increff.pos.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UploadResponse {
    private String status;
    private String tsvBase64;
    private String filename;
}
