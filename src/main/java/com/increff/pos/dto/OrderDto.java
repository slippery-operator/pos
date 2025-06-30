package com.increff.pos.dto;

import com.increff.pos.flow.OrderFlow;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.form.OrderSearchForm;
import com.increff.pos.model.response.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class OrderDto {

    @Autowired
    private OrderFlow orderProcessingFlow;

    public OrderResponse createOrders(List<OrderItemForm> orderItems) {
        OrderForm orderForm = new OrderForm();
        orderForm.setOrderItems(orderItems);
        return orderProcessingFlow.createOrderFromBarcodes(orderForm);
    }

    public List<OrderResponse> searchOrders(OrderSearchForm searchForm) {
        ZonedDateTime startDate = searchForm.getStartDate() != null ?
                searchForm.getStartDate().atStartOfDay(java.time.ZoneOffset.UTC) : null;
        ZonedDateTime endDate = searchForm.getEndDate() != null ?
                searchForm.getEndDate().atTime(23, 59, 59).atZone(java.time.ZoneOffset.UTC) : null;

        return orderProcessingFlow.searchOrders(startDate, endDate, searchForm.getOrderId());
    }

    public OrderResponse getOrderById(Integer orderId) {
        return orderProcessingFlow.getOrderWithItems(orderId);
    }
}