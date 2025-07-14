package com.increff.pos.model.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderWithInvoiceResponse {

    private Integer id;
    private ZonedDateTime time;
    private Double totalRevenue;
    private List<OrderItemInvoiceResponse> orderItems;
} 