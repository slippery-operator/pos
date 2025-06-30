package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.entity.OrderItemPojo;
import com.increff.pos.entity.OrderPojo;
import com.increff.pos.exception.EntityNotFoundException;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.response.OrderItemResponse;
import com.increff.pos.model.response.OrderResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    public List<OrderResponse> searchOrders(ZonedDateTime startDate, ZonedDateTime endDate, Integer orderId) {
        List<OrderPojo> orders = orderDao.findBySearchCriteria(startDate, endDate, orderId);
        return orders.stream()
                .map(this::convertToOrderResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse getOrderById(Integer id) {
        OrderPojo order = orderDao.selectById(id);
        if (order == null) {
            throw new EntityNotFoundException("Order not found with id: " + id);
        }
        return convertToOrderResponse(order);
    }

    public OrderResponse createOrder() {
        OrderPojo order = new OrderPojo();
        order.setTime(ZonedDateTime.now());
        orderDao.insert(order);
        return convertToOrderResponse(order);
    }

    public void updateInvoicePath(Integer orderId, String invoicePath) {
        OrderPojo order = orderDao.selectById(orderId);
        if (order == null) {
            throw new EntityNotFoundException("Order not found with id: " + orderId);
        }
        orderDao.updateInvoicePath(orderId, invoicePath);
    }

    private OrderResponse convertToOrderResponse(OrderPojo order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setTime(order.getTime());
//        response.setInvoicePath(order.getInvoicePath());
//        response.setVersion(order.getVersion());
//        response.setCreatedAt(order.getCreatedAt());
//        response.setUpdatedAt(order.getUpdatedAt());
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            List<OrderItemResponse> orderItemResponses = order.getOrderItems().stream()
                    .map(this::convertOrderItemToResponse)
                    .collect(Collectors.toList());
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