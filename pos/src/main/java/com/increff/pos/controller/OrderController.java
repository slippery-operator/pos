package com.increff.pos.controller;

import com.increff.pos.dto.OrderDto;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.response.OrderResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderDto orderDto;

    @GetMapping
    @ApiOperation(value = "Search orders by date range and order ID")
    public List<OrderResponse> searchOrders(
            @ApiParam(value = "Start date (YYYY-MM-DD)")
            @RequestParam(required = false, name = "start-date") String startDate,
            @ApiParam(value = "End date (YYYY-MM-DD)")
            @RequestParam(required = false, name = "end-date") String endDate,
            @ApiParam(value = "Order ID")
            @RequestParam(required = false, name = "order-id") Integer orderId) {

        //TODO: move these to DTO

        return orderDto.searchOrders(startDate, endDate, orderId);
    }

    //TODO: to remove mediatype
    @PostMapping
    @ApiOperation(value = "Create order from list of order items")
    public OrderResponse createOrders(
            @ApiParam(value = "List of order items", required = true)
            @Valid @RequestBody List<OrderItemForm> orderItems) {
        return orderDto.createOrders(orderItems);
    }

    @GetMapping("/{id}/order-items")
    @ApiOperation(value = "Get order details with items by order ID")
    public OrderResponse getOrderById(
            @ApiParam(value = "Order ID", required = true)
            @PathVariable Integer id) {
        return orderDto.getOrderById(id);
    }
}