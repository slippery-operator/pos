//// OrderApi.java
//package com.increff.pos.service;
//
//import com.increff.pos.dao.OrderDao;
//
//import com.increff.pos.entity.OrderPojo;
//import com.increff.pos.exception.ApiException;
//
//import com.increff.pos.model.form.OrderForm;
//import com.increff.pos.model.response.OrderResponse;
//import com.increff.pos.util.ConverterUtil;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.ZonedDateTime;
//import java.util.List;
//import java.util.stream.Collectors;
//
//@Service
//@Transactional
//public class OrderApi {
//    @Autowired
//    private OrderDao orderDao;
//
//    public OrderResponse createOrder(OrderForm orderForm) {
//        OrderPojo order = new OrderPojo();
//        order.setTime(ZonedDateTime.now());
//        orderDao.insert(order);
//        return ConverterUtil.convert(order);
//    }
//
//    public OrderResponse getOrderById(Integer id) throws ApiException {
//        OrderPojo order = orderDao.selectById(id);
//        if (order == null) {
//            throw new ApiException("Order with ID " + id + " not found");
//        }
//        return ConverterUtil.convert(order);
//    }
//
//    public List<OrderResponse> getOrdersByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
//        return orderDao.selectByDateRange(startDate, endDate).stream()
//                .map(ConverterUtil::convert)
//                .collect(Collectors.toList());
//    }
//
//    public void updateInvoicePath(Integer orderId, String invoicePath) throws ApiException {
//        OrderPojo order = orderDao.selectById(orderId);
//        if (order == null) {
//            throw new ApiException("Order with ID " + orderId + " not found");
//        }
//        order.setInvoicePath(invoicePath);
//        orderDao.update(order);
//    }
//}