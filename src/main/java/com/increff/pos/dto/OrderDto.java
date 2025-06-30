package com.increff.pos.dto;

import com.increff.pos.flow.OrderFlow;
import com.increff.pos.entity.OrderItemPojo;
import com.increff.pos.entity.OrderPojo;
import com.increff.pos.exception.EntityNotFoundException;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.response.OrderItemResponse;
import com.increff.pos.model.response.OrderResponse;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.api.ProductApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class OrderDto {

    @Autowired
    private OrderFlow orderFlow;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private Validator validator;

    public OrderResponse createOrders(List<OrderItemForm> orderItems) {
        // Validate all order items
        validateOrderItems(orderItems);

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
        OrderPojo createdOrder = orderFlow.createOrderFromBarcodes(barcodes, quantities, mrps);

        // Get order items and convert to response
        List<OrderItemPojo> orderItemPojos = orderFlow.getOrderItemsByOrderId(createdOrder.getId());

        return convertToOrderResponse(createdOrder, orderItemPojos);
    }

    public List<OrderResponse> searchOrders(LocalDate startDate, LocalDate endDate, Integer orderId) {
        // Convert LocalDate to ZonedDateTime
        ZonedDateTime startDateTime = startDate != null ?
                startDate.atStartOfDay(java.time.ZoneOffset.UTC) : null;
        ZonedDateTime endDateTime = endDate != null ?
                endDate.atTime(23, 59, 59).atZone(java.time.ZoneOffset.UTC) : null;

        // Call flow layer with parameters
        List<OrderPojo> orders = orderFlow.searchOrders(startDateTime, endDateTime, orderId);

        // Convert to response
        return orders.stream()
                .map(order -> convertToOrderResponse(order, null))
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Integer orderId) {
        // Get order and items from flow
        OrderPojo order = orderFlow.getOrderWithItems(orderId);
        List<OrderItemPojo> orderItems = orderFlow.getOrderItemsByOrderId(orderId);

        return convertToOrderResponse(order, orderItems);
    }

    private void validateOrderItems(List<OrderItemForm> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("Order items cannot be empty");
        }

        for (OrderItemForm orderItem : orderItems) {
            Set<ConstraintViolation<OrderItemForm>> violations = validator.validate(orderItem);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                throw new IllegalArgumentException("Validation failed: " + errorMessage);
            }
        }
    }

    private OrderResponse convertToOrderResponse(OrderPojo order, List<OrderItemPojo> orderItems) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTime(order.getTime());

        if (orderItems != null && !orderItems.isEmpty()) {
            List<OrderItemResponse> orderItemResponses = new ArrayList<>();
            for (OrderItemPojo orderItem : orderItems) {
                OrderItemResponse itemResponse = convertOrderItemToResponse(orderItem);

//                // Enhance with product information
//                try {
//                    ProductResponse product = productApi.getProductById(orderItem.getProductId());
//                    itemResponse.setProductName(product.getName());
//                    itemResponse.setBarcode(product.getBarcode());
//                } catch (EntityNotFoundException e) {
//                    itemResponse.setProductName("Product Not Found");
//                    itemResponse.setBarcode("N/A");
//                }

                orderItemResponses.add(itemResponse);
            }
            response.setOrderItems(orderItemResponses);
        }

        return response;
    }

    private OrderItemResponse convertOrderItemToResponse(OrderItemPojo orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setOrderId(orderItem.getOrderId());
        response.setProductId(orderItem.getProductId());
        response.setQuantity(orderItem.getQuantity());
        response.setSellingPrice(orderItem.getSellingPrice());
        return response;
    }
}