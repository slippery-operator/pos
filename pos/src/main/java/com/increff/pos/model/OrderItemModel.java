package com.increff.pos.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Model class to encapsulate order item data for better readability and to avoid
 * potential index mismatches when using separate lists of barcodes, quantities, and MRPs.
 * This ensures that related data stays together and reduces the risk of errors.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemModel {
    
    private String barcode;
    private Integer quantity;
    private Double mrp;

    @Override
    public String toString() {
        return "OrderItemModel{" +
                "barcode='" + barcode + '\'' +
                ", quantity=" + quantity +
                ", mrp=" + mrp +
                '}';
    }
} 