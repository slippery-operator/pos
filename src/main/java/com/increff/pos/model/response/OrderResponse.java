package com.increff.pos.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Integer id;
    private Instant time;
//    private String invoicePath;
    private List<OrderItemResponse> orderItems;
}
