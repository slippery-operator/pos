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
        if (orderId == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order ID cannot be null");
        }
        return orderItemDao.getOrderItemsByOrderId(orderId);
    }

    /**
     * Create a new order item
     */
    public OrderItemsPojo createOrderItem(OrderItemsPojo orderItem) {
        validateOrderItem(orderItem);
        orderItemDao.insert(orderItem);
        return orderItem;
    }

    /**
     * Bulk create order items
     */
    public List<OrderItemsPojo> bulkCreateOrderItems(List<OrderItemsPojo> orderItems) {
        for (OrderItemsPojo item : orderItems) {
            validateOrderItem(item);
        }
        orderItemDao.bulkInsert(orderItems);
        return orderItems;
    }

    /**
     * Get order item by ID
     */
    public OrderItemsPojo getOrderItemById(Integer id) {
        OrderItemsPojo orderItem = orderItemDao.selectById(id);
        if (orderItem == null) {
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND, 
                "Order item not found with id: " + id);
        }
        return orderItem;
    }

    /**
     * Update order item
     */
    public OrderItemsPojo updateOrderItem(Integer id, OrderItemsPojo orderItem) {
        OrderItemsPojo existing = getOrderItemById(id);
        validateOrderItem(orderItem);
        
        existing.setOrderId(orderItem.getOrderId());
        existing.setProductId(orderItem.getProductId());
        existing.setQuantity(orderItem.getQuantity());
        existing.setSellingPrice(orderItem.getSellingPrice());
        
        orderItemDao.update(existing);
        return existing;
    }

    /**
     * Delete order item
     */
    public void deleteOrderItem(Integer id) {
        OrderItemsPojo orderItem = getOrderItemById(id);
        orderItemDao.delete(orderItem);
    }

    /**
     * Validate order item data
     */
    private void validateOrderItem(OrderItemsPojo orderItem) {
        if (orderItem.getOrderId() == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Order ID cannot be null");
        }
        if (orderItem.getProductId() == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Product ID cannot be null");
        }
        if (orderItem.getQuantity() == null || orderItem.getQuantity() <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Quantity must be positive");
        }
        if (orderItem.getSellingPrice() == null || orderItem.getSellingPrice() <= 0) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Selling price must be positive");
        }
    }
}