package com.increff.pos.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response class for invoice generation from external service
 * Contains the generated PDF data and status information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceGenerationResponse {

    private Integer orderId;
    private String base64Pdf;
    private String message;
    private boolean success;
} 