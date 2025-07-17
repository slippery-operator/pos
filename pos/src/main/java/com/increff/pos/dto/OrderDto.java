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
import com.increff.pos.util.DateUtil;
import com.increff.pos.api.ProductApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.increff.pos.util.DateUtil.format;

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
        validationUtil.validateForms(orderItems);
        OrdersPojo createdOrder = orderFlow.createOrder(orderItems);
        List<OrderItemsPojo> orderItemPojos = orderItemApi.getOrderItemsByOrderId(createdOrder.getId());
        return convertToOrderResponse(createdOrder, orderItemPojos);
    }

    public List<OrderResponse> searchOrders(String startDate, String endDate, Integer orderId, int page, int size) {
        ZonedDateTime parsedStartDate = DateUtil.parseStartDate(startDate);
        ZonedDateTime parsedEndDate = DateUtil.parseEndDate(endDate);
        List<OrdersPojo> orders = orderApi.searchOrders(parsedStartDate, parsedEndDate, orderId, page, size);
        List<OrderResponse> retrievedOrders = orders.stream()
                .map(order -> convertToOrderResponse(order, orderItemApi.getOrderItemsByOrderId(order.getId())))
                .collect(Collectors.toList());

        return retrievedOrders;
    }

    public OrderResponse getOrderById(Integer orderId) {
        validateId(orderId, "order Id");
        OrdersPojo order = orderApi.getOrderById(orderId);
        List<OrderItemsPojo> orderItems = orderItemApi.getOrderItemsByOrderId(orderId);
        return convertToOrderResponse(order, orderItems);
    }

    private OrderResponse convertToOrderResponse(OrdersPojo order, List<OrderItemsPojo> orderItems) {
        OrderResponse response = convertUtil.convert(order, OrderResponse.class);
        response.setFormattedTime(format(order.getTime()));
        if (orderItems != null && !orderItems.isEmpty()) {
            List<OrderItemResponse> orderItemResponses = convertUtil.convertList(orderItems, OrderItemResponse.class);
            response.setOrderItems(orderItemResponses);
        }
        return response;
    }
}