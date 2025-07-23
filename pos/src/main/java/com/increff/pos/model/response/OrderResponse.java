package com.increff.pos.model.response;

import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Integer id;
    private ZonedDateTime time;
    //TODO: check Jackson time converter
    private String formattedTime;
    private List<OrderItemResponse> orderItems;
}
