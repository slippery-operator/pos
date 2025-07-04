package com.increff.pos.model.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

/**
 * Form class for invoice item data
 * Used in invoice generation requests for external service
 */
@Getter
@Setter
@NoArgsConstructor
public class InvoiceItemForm {

    @NotBlank(message = "Product name cannot be empty")
    private String productName;

    @NotBlank(message = "Barcode cannot be empty")
    private String barcode;

    @NotNull(message = "Quantity cannot be null")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "MRP cannot be null")
    @Positive(message = "MRP must be positive")
    private Double mrp;

    @NotNull(message = "Total amount cannot be null")
    @Positive(message = "Total amount must be positive")
    private Double totalAmount;
} 