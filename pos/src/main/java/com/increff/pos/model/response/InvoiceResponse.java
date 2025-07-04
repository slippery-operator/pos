package com.increff.pos.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * Response class for invoice data in POS app
 * Contains invoice information for API responses
 */
@Getter
@Setter
@NoArgsConstructor
public class InvoiceResponse {

    private Integer id;
    private Integer orderId;
    private String invoiceNumber;
    private Instant timeStamp;
    private Integer countOfItems;
    private String invoicePath;
    private Double finalRevenue;
} 