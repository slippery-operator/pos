package com.increff.invoice.model.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class OrderRequest {

    @NotNull(message = "Order ID cannot be null")
    private Integer orderId;

    @NotNull(message = "Order time cannot be null")
    private String orderTime;

    @NotNull(message = "Client name cannot be null")
    @Size(min = 1, max = 100, message = "Client name must be between 1 and 100 characters")
    private String clientName;

    @Valid
    @NotNull(message = "Order items cannot be null")
    @Size(min = 1, message = "Order must have at least one item")
    private List<OrderItemRequest> orderItems;

    @NotNull(message = "Total revenue cannot be null")
    private Double totalRevenue;

    @Getter
    @Setter
    @NoArgsConstructor
    public static class OrderItemRequest {
        
        @NotNull(message = "Product name cannot be null")
        @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
        private String productName;

        @NotNull(message = "Barcode cannot be null")
        @Size(min = 1, max = 50, message = "Barcode must be between 1 and 50 characters")
        private String barcode;

        @NotNull(message = "Quantity cannot be null")
        private Integer quantity;

        @NotNull(message = "MRP cannot be null")
        private Double mrp;

        @NotNull(message = "Total amount cannot be null")
        private Double totalAmount;
    }
} 