package com.increff.pos.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

/**
 * Response class for order data with invoice information
 * Contains order information with additional invoice-related fields
 */
@Getter
@Setter
@NoArgsConstructor
public class OrderWithInvoiceResponse {

    private Integer id;
    private Instant time;
    private Double totalRevenue;
    private List<OrderItemInvoiceResponse> orderItems;

    /**
     * Constructor for order with invoice response
     */
    public OrderWithInvoiceResponse(Integer id, Instant time, List<OrderItemInvoiceResponse> orderItems, Double totalRevenue) {
        this.id = id;
        this.time = time;
        this.orderItems = orderItems;
        this.totalRevenue = totalRevenue;
    }
} 