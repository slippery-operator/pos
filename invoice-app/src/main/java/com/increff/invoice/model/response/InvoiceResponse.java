package com.increff.invoice.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response class for invoice generation
 * Returns base64 encoded PDF string to POS application
 */
@Getter
@Setter
@NoArgsConstructor
public class InvoiceResponse {

    private Integer orderId;
    private String invoiceNumber;
    private String base64Pdf;
    private String message;
    private boolean success;

    /**
     * Constructor for successful invoice generation
     */
    public InvoiceResponse(Integer orderId, String invoiceNumber, String base64Pdf) {
        this.orderId = orderId;
        this.invoiceNumber = invoiceNumber;
        this.base64Pdf = base64Pdf;
        this.success = true;
        this.message = "Invoice generated successfully";
    }

    /**
     * Constructor for failed invoice generation
     */
    public InvoiceResponse(Integer orderId, String message) {
        this.orderId = orderId;
        this.message = message;
        this.success = false;
    }
} 