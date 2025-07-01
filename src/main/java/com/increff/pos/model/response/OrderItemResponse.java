package com.increff.pos.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemResponse {
    private Integer id;
    private Integer orderId;
    private Integer productId;
//    private String productName;
//    private String barcode;
    private Integer quantity;
    private Double sellingPrice;
}
