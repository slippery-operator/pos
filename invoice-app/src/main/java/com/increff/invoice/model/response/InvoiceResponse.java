package com.increff.invoice.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InvoiceResponse {

    private Integer orderId;
    private String base64Pdf;
    private String message;
    private boolean success;

    public InvoiceResponse(Integer orderId, String base64Pdf, boolean success) {
        this.orderId = orderId;
        this.base64Pdf = base64Pdf;
        this.success = true;
        this.message = "Invoice generated successfully";
    }
} 