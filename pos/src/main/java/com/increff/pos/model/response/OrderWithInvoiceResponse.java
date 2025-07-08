package com.increff.pos.model.response;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class OrderWithInvoiceResponse {

    private Integer id;
    private Instant time;
    private Double totalRevenue;
    private List<OrderItemInvoiceResponse> orderItems;
} 