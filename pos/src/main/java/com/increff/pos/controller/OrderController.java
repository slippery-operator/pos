package com.increff.pos.controller;

import com.increff.pos.dto.OrderDto;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.response.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderDto dto;

    @GetMapping
    public List<OrderResponse> searchOrders(
            @RequestParam(required = false, name = "start-date") String startDate,
            @RequestParam(required = false, name = "end-date") String endDate,
            @RequestParam(required = false, name = "order-id") Integer orderId) {
        return dto.searchOrders(startDate, endDate, orderId);
    }

    @PostMapping
    public OrderResponse createOrders(@Valid @RequestBody List<OrderItemForm> orderItems) {
        return dto.createOrders(orderItems);
    }

    @GetMapping("/{id}/order-items")
    public OrderResponse getOrderById(@PathVariable Integer id) {
        return dto.getOrderById(id);
    }
}