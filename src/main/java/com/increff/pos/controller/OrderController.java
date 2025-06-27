//package com.increff.pos.controller;
//
//
//import com.increff.pos.model.response.OrderResponse;
//import com.increff.pos.model.response.ProductResponse;
//import io.swagger.annotations.ApiOperation;
//import io.swagger.annotations.ApiParam;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.MediaType;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RequestPart;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/orders")
//public class OrderController {
//
//    @Autowired
//    private OrderDto orderDto;
//
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @ApiOperation(value = "Upload products from TSV file", consumes = "multipart/form-data")
//    public List<OrderResponse> createOrdersTsv(
//            @ApiParam(value = "TSV file containing order data", required = true)
//            @RequestPart(value = "file") MultipartFile file) {
//        return orderDto.createOrdersTsv(file);
//    }
//}
