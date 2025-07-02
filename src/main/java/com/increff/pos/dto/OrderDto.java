package com.increff.pos.dto;

import com.increff.pos.api.OrderApi;
import com.increff.pos.exception.ValidationException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.response.OrderItemResponse;
import com.increff.pos.model.response.OrderResponse;
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

    @Autowired
    private OrderApi orderApi;

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

        List<OrderResponse> retrievedOrders = orders.stream()
                .map(order -> convertToOrderResponse(order, orderFlow.getOrderItemsByOrderId(order.getId())))
                .collect(Collectors.toList());

        return retrievedOrders;
    }

    public OrderResponse getOrderById(Integer orderId) {
        // Get order and items from flow
        OrdersPojo order = orderFlow.getOrderWithItems(orderId);
        List<OrderItemsPojo> orderItems = orderFlow.getOrderItemsByOrderId(orderId);

        return convertToOrderResponse(order, orderItems);
    }

    private void validateOrderItems(List<OrderItemForm> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new ValidationException("Order items cannot be empty");
        }

        for (OrderItemForm orderItem : orderItems) {
            Set<ConstraintViolation<OrderItemForm>> violations = validator.validate(orderItem);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                        .map(ConstraintViolation::getMessage)
                        .collect(Collectors.joining(", "));
                throw new ValidationException("Validation failed: " + errorMessage);
            }
        }
    }

    private OrderResponse convertToOrderResponse(OrdersPojo order, List<OrderItemsPojo> orderItems) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTime(order.getTime());

        if (orderItems != null && !orderItems.isEmpty()) {
            List<OrderItemResponse> orderItemResponses = new ArrayList<>();
            for (OrderItemsPojo orderItem : orderItems) {
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

    private OrderItemResponse convertOrderItemToResponse(OrderItemsPojo orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setOrderId(orderItem.getOrderId());
        response.setProductId(orderItem.getProductId());
        response.setQuantity(orderItem.getQuantity());
        response.setSellingPrice(orderItem.getSellingPrice());
        return response;
    }
}