package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Transactional
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    public List<OrdersPojo> searchOrders(ZonedDateTime startDate, ZonedDateTime endDate, Integer orderId, int page, int size) {
        return orderDao.findBySearchCriteria(startDate, endDate, orderId, page, size);
    }

    public OrdersPojo getOrderById(Integer id) {
        OrdersPojo order = orderDao.selectById(id);
        if (order == null) {
            throw new ApiException(ErrorType.NOT_FOUND, "Order not found");
        }
        return order;
    }

    public OrdersPojo createOrder() {
        OrdersPojo order = new OrdersPojo();
        orderDao.insert(order);
        return order;
    }

    public void updateInvoicePath(Integer orderId, String invoicePath) {
        OrdersPojo order = orderDao.selectById(orderId);
        if (order == null) {
            throw new ApiException(ErrorType.NOT_FOUND,
                "Order not found");
        }
        orderDao.updateInvoicePath(orderId, invoicePath);
    }

    // Removed getOrderItemsByOrderId - this should be called from OrderItemApi directly
}