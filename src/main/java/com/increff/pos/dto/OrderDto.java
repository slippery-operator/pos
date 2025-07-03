package com.increff.pos.dto;

import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
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

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
    private OrderItemApi orderItemApi;

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

        // Call flow layer with extracted parameters (complex orchestration)
        OrdersPojo createdOrder = orderFlow.createOrderFromBarcodes(barcodes, quantities, mrps);

        // Get order items and convert to response
        List<OrderItemsPojo> orderItemPojos = orderItemApi.getOrderItemsByOrderId(createdOrder.getId());

        return convertToOrderResponse(createdOrder, orderItemPojos);
    }

    public List<OrderResponse> searchOrders(String startDate, String endDate, Integer orderId) {

        Instant parsedStartDate = null;
        Instant parsedEndDate = null;

        if (startDate != null && !startDate.isEmpty()) {
            parsedStartDate = LocalDate.parse(startDate)
                    .atStartOfDay(ZoneOffset.UTC)
                    .toInstant(); // 00:00 UTC
        }

        if (endDate != null && !endDate.isEmpty()) {
            parsedEndDate = LocalDate.parse(endDate)
                    .plusDays(1)                              // move to next day
                    .atStartOfDay(ZoneOffset.UTC)             // at 00:00 UTC
                    .toInstant()
                    .minusMillis(1);                          // back to 23:59:59.999
        }

        // Direct API call instead of using flow layer
        List<OrdersPojo> orders = orderApi.searchOrders(parsedStartDate, parsedEndDate, orderId);

        List<OrderResponse> retrievedOrders = orders.stream()
                .map(order -> convertToOrderResponse(order, orderItemApi.getOrderItemsByOrderId(order.getId())))
                .collect(Collectors.toList());

        return retrievedOrders;
    }

    public OrderResponse getOrderById(Integer orderId) {
        validateId(orderId, "order Id");

        // Direct API calls instead of using flow layer
        OrdersPojo order = orderApi.getOrderById(orderId);
        List<OrderItemsPojo> orderItems = orderItemApi.getOrderItemsByOrderId(orderId);

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