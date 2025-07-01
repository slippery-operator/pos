package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.exception.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.List;

@Service
@Transactional
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    public List<OrdersPojo> searchOrders(ZonedDateTime startDate, ZonedDateTime endDate, Integer orderId) {
        return orderDao.findBySearchCriteria(startDate, endDate, orderId);
    }

    public OrdersPojo getOrderById(Integer id) {
        OrdersPojo order = orderDao.selectById(id);
        if (order == null) {
            throw new EntityNotFoundException("Order not found with id: " + id);
        }
        return order;
    }

    public OrdersPojo createOrder() {
        OrdersPojo order = new OrdersPojo();
        order.setTime(ZonedDateTime.now());
        orderDao.insert(order);
        return order;
    }

    public void updateInvoicePath(Integer orderId, String invoicePath) {
        OrdersPojo order = orderDao.selectById(orderId);
        if (order == null) {
            throw new EntityNotFoundException("Order not found with id: " + orderId);
        }
        orderDao.updateInvoicePath(orderId, invoicePath);
    }
}