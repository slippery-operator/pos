//// OrderItemApi.java
//package com.increff.pos.service;
//
//import com.increff.pos.dao.OrderItemDao;
//import com.increff.pos.dao.ProductDao;
//import com.increff.pos.entity.OrderItemPojo;
//import com.increff.pos.entity.ProductPojo;
//import com.increff.pos.entity.ProductPojo;
//import com.increff.pos.exception.ApiException;
//import com.increff.pos.model.data.OrderItemData;
//import com.increff.pos.model.form.OrderItemForm;
//import com.increff.pos.model.response.OrderItemResponse;
//import com.increff.pos.util.ConverterUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//public class OrderItemApi {
//    @Autowired
//    private OrderItemDao orderItemDao;
//    @Autowired
//    private ProductDao productDao;
//
//    public void bulkCreateOrderItems(List<OrderItemForm> orderItemForms) throws ApiException {
//        for (OrderItemForm form : orderItemForms) {
//            createOrderItem(form);
//        }
//    }
//
//    public OrderItemResponse createOrderItem(OrderItemForm orderItemForm) throws ApiException {
//        ProductPojo product = productDao.selectById(orderItemForm.getProductId());
//        if (product == null) {
//            throw new ApiException("Product with ID " + orderItemForm.getProductId() + " not found");
//        }
//
//        OrderItemPojo orderItem = new OrderItemPojo();
//        orderItem.setOrderId(orderItemForm.getOrderId());
//        orderItem.setProductId(orderItemForm.getProductId());
//        orderItem.setQuantity(orderItemForm.getQuantity());
//        orderItem.setSellingPrice(orderItemForm.getMrp());
//        orderItemDao.insert(orderItem);
//
//        return ConverterUtil.convert(orderItem, product);
//    }
//
//    public List<OrderItemResponse> getOrderItemsByOrderId(Integer orderId) {
//        List<OrderItemPojo> orderItems = orderItemDao.selectByOrderId(orderId);
//        return orderItems.stream().map(item -> {
//            ProductPojo product = productDao.selectById(item.getProductId());
//            return ConverterUtil.convert(item, product);
//        }).collect(Collectors.toList());
//    }
//}