package com.increff.pos.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response class for order item data with invoice information
 * Contains product details needed for invoice generation
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemInvoiceResponse {

    private String productName;
    private String barcode;
    private Integer quantity;
    private Double sellingPrice;
} 