package com.increff.pos.dto;

import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.model.OrderItemModel;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.response.OrderItemResponse;
import com.increff.pos.model.response.OrderResponse;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.DateUtil;
import com.increff.pos.api.ProductApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
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

        // Convert forms to OrderItemModel for better readability and to avoid index mismatches
        // This ensures that barcode, quantity, and MRP stay together as a unit
        List<OrderItemModel> orderItemModels = orderItems.stream()
                .map(form -> new OrderItemModel(form.getBarcode(), form.getQuantity(), form.getMrp()))
                .collect(Collectors.toList());

        // Call flow layer with OrderItemModel list (complex orchestration)
        OrdersPojo createdOrder = orderFlow.createOrder(orderItemModels);

        // Get order items and convert to response
        List<OrderItemsPojo> orderItemPojos = orderItemApi.getOrderItemsByOrderId(createdOrder.getId());

        return convertToOrderResponse(createdOrder, orderItemPojos);
    }

    public List<OrderResponse> searchOrders(String startDate, String endDate, Integer orderId) {

        // Parse date strings using DateUtil for better organization and reusability
        Instant parsedStartDate = DateUtil.parseStartDate(startDate);
        Instant parsedEndDate = DateUtil.parseEndDate(endDate);

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

    /**
     * Converts OrdersPojo and its associated OrderItemsPojo list to OrderResponse.
     * This method is kept in the DTO layer as it requires access to ConvertUtil dependency
     * and is specific to the order domain conversion logic.
     * 
     * @param order The order entity to convert
     * @param orderItems The list of order items associated with the order
     * @return OrderResponse containing the converted order and its items
     */
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