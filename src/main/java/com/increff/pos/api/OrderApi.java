package com.increff.pos.api;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class OrderApi {

    @Autowired
    private OrderDao orderDao;

    public List<OrdersPojo> searchOrders(Instant startDate, Instant endDate, Integer orderId) {
        return orderDao.findBySearchCriteria(startDate, endDate, orderId);
    }

    public OrdersPojo getOrderById(Integer id) {
        OrdersPojo order = orderDao.selectById(id);
        if (order == null) {
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND, 
                "Order not found with id: " + id);
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
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND, 
                "Order not found with id: " + orderId);
        }
        orderDao.updateInvoicePath(orderId, invoicePath);
    }
}