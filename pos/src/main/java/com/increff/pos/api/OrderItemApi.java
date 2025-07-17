package com.increff.pos.api;

import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.exception.ApiException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class OrderItemApi {

    @Autowired
    private OrderItemDao orderItemDao;

    public List<OrderItemsPojo> getOrderItemsByOrderId(Integer orderId) {
        return orderItemDao.selectByOrderId(orderId);
    }

    public List<OrderItemsPojo> createOrderItemsGroup(List<OrderItemsPojo> orderItems) {
        orderItemDao.insertGroup(orderItems);
        return orderItems;
    }
}