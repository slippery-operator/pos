package com.increff.pos.api;

import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.exception.ApiException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * API layer for OrderItem entity operations
 * Handles business logic and data validation for order items
 */
@Service
@Transactional
public class OrderItemApi {

    @Autowired
    private OrderItemDao orderItemDao;

    /**
     * Get order items by order ID
     */
    public List<OrderItemsPojo> getOrderItemsByOrderId(Integer orderId) {
        return orderItemDao.selectByOrderId(orderId);
    }

    /**
     * Bulk create order items
     */
    public List<OrderItemsPojo> createOrderItemsGroup(List<OrderItemsPojo> orderItems) {
        orderItemDao.insertGroup(orderItems);
        return orderItems;
    }
}