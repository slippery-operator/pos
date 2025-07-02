package com.increff.pos.dto;

import com.increff.pos.api.OrderApi;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.response.OrderItemResponse;
import com.increff.pos.model.response.OrderResponse;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.api.ProductApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderDto extends AbstractDto<OrderItemForm> {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private ConvertUtil convertUtil;

    public OrderResponse createOrders(List<OrderItemForm> orderItems) {
        // Validate all order items
        validationUtil.validateForms(orderItems);

        // Extract data from forms
        List<String> barcodes = orderItems.stream()
                .map(OrderItemForm::getBarcode)
                .collect(Collectors.toList());

        List<Integer> quantities = orderItems.stream()
                .map(OrderItemForm::getQuantity)
                .collect(Collectors.toList());

        List<Double> mrps = orderItems.stream()
                .map(OrderItemForm::getMrp)
                .collect(Collectors.toList());

        // Call flow layer with extracted parameters
        OrdersPojo createdOrder = orderFlow.createOrderFromBarcodes(barcodes, quantities, mrps);

        // Get order items and convert to response
        List<OrderItemsPojo> orderItemPojos = orderFlow.getOrderItemsByOrderId(createdOrder.getId());

        return convertToOrderResponse(createdOrder, orderItemPojos);
    }

    public List<OrderResponse> searchOrders(String startDate, String endDate, Integer orderId) {

        LocalDate parsedStartDate = null;
        LocalDate parsedEndDate = null;

        if (startDate != null && !startDate.isEmpty()) {
            parsedStartDate = LocalDate.parse(startDate);
        }
        if (endDate != null && !endDate.isEmpty()) {
            parsedEndDate = LocalDate.parse(endDate);
        }

        // Convert LocalDate to ZonedDateTime
        ZonedDateTime startDateTime = parsedStartDate != null ?
                parsedStartDate.atStartOfDay(java.time.ZoneOffset.UTC) : null;
        ZonedDateTime endDateTime = parsedEndDate != null ?
                parsedEndDate.plusDays(1).atStartOfDay(java.time.ZoneOffset.UTC) : null;

        // Call flow layer with parameters
        List<OrdersPojo> orders = orderFlow.searchOrders(startDateTime, endDateTime, orderId);

        System.out.println(orders);

        List<OrderResponse> retrievedOrders = orders.stream()
                .map(order -> convertToOrderResponse(order, orderFlow.getOrderItemsByOrderId(order.getId())))
                .collect(Collectors.toList());
        System.out.println(retrievedOrders);

        return retrievedOrders;
    }

    public OrderResponse getOrderById(Integer orderId) {
        validateId(orderId, "order Id");

        // Get order and items from flow
        OrdersPojo order = orderFlow.getOrderWithItems(orderId);
        List<OrderItemsPojo> orderItems = orderFlow.getOrderItemsByOrderId(orderId);

        return convertToOrderResponse(order, orderItems);
    }

    @Override
    protected void validateForm(OrderItemForm form) {
        validationUtil.validateForm(form);
    }

    private OrderResponse convertToOrderResponse(OrdersPojo order, List<OrderItemsPojo> orderItems) {
        // Convert base order using ConvertUtil
        OrderResponse response = convertUtil.convert(order, OrderResponse.class);

        // Convert order items using ConvertUtil
        if (orderItems != null && !orderItems.isEmpty()) {
            List<OrderItemResponse> orderItemResponses = convertUtil.convertList(orderItems, OrderItemResponse.class);
            response.setOrderItems(orderItemResponses);
        }

        return response;
    }
}