package com.increff.pos.api;

import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.OrderItemPojo;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.response.OrderItemResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderItemApi {

    @Autowired
    private OrderItemDao orderItemDao;

    public List<OrderItemResponse> getOrderItemsByOrderId(Integer orderId) {
        List<OrderItemPojo> orderItems = orderItemDao.selectByOrderId(orderId);
        return orderItems.stream()
                .map(this::convertToOrderItemResponse)
                .collect(Collectors.toList());
    }

    public void bulkCreateOrderItems(List<OrderItemForm> orderItemForms) {
        List<OrderItemPojo> orderItems = orderItemForms.stream()
                .map(this::convertToOrderItemPojo)
                .collect(Collectors.toList());
        orderItemDao.bulkInsert(orderItems);
    }

    private OrderItemPojo convertToOrderItemPojo(OrderItemForm form) {
        OrderItemPojo orderItem = new OrderItemPojo();
        orderItem.setOrderId(form.getOrderId());
        orderItem.setProductId(form.getProductId());
        orderItem.setQuantity(form.getQuantity());
        orderItem.setSellingPrice(form.getMrp());
        return orderItem;
    }

    private OrderItemResponse convertToOrderItemResponse(OrderItemPojo orderItem) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(orderItem.getId());
        response.setOrderId(orderItem.getOrderId());
        response.setProductId(orderItem.getProductId());
        response.setQuantity(orderItem.getQuantity());
        response.setSellingPrice(orderItem.getSellingPrice());
//        response.setVersion(orderItem.getVersion());
//        response.setCreatedAt(orderItem.getCreatedAt());
//        response.setUpdatedAt(orderItem.getUpdatedAt());
        return response;
    }
}