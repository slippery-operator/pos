package com.increff.pos.model.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;
import java.util.List;

@Getter
@Setter
public class OrderResponse {
    private Integer id;
    @JsonFormat(pattern = "dd MMM yyyy, h:mm a z", timezone = "UTC")
    private ZonedDateTime time;
    private List<OrderItemResponse> orderItems;
}
