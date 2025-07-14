package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.form.OrderItemForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderFlow {

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Transactional
    public OrdersPojo createOrder(List<OrderItemForm> orderItems) {
        Map<String, Integer> productBarcodeToId =  validateOrderCreation(orderItems);
        OrdersPojo createdOrder = orderApi.createOrder();
        List<OrderItemsPojo> orderItemsToCreate = constructOrderItemList(orderItems, productBarcodeToId, createdOrder.getId());
        orderItemApi.createOrderItemsGroup(orderItemsToCreate);
        return createdOrder;
    }

    private Map<String, Integer> validateOrderCreation(List<OrderItemForm> orderItems) {
        List<String> barcodes = orderItems.stream().map(OrderItemForm::getBarcode).collect(Collectors.toList());
        Map<String, Integer> productBarcodeToId = productApi.findProductsByBarcodes(barcodes);
        for (OrderItemForm orderItem : orderItems) {
            if (!productBarcodeToId.containsKey(orderItem.getBarcode())) {
                throw new ApiException(ErrorType.NOT_FOUND, "Product not found");
            }
        }
        for (OrderItemForm orderItem : orderItems) {
            Integer productId = productBarcodeToId.get(orderItem.getBarcode());
            inventoryApi.validateInventoryAvailability(productId, orderItem.getQuantity());
        }
        return productBarcodeToId;
    }

    private List<OrderItemsPojo> constructOrderItemList(List<OrderItemForm> orderItemForms,
                                                        Map<String, Integer> productBarcodeToId, Integer orderId) {
        List<OrderItemsPojo> orderItemsToCreate = new ArrayList<>();
        for (OrderItemForm orderItem : orderItemForms) {
            Integer productId = productBarcodeToId.get(orderItem.getBarcode());
            inventoryApi.reduceInventory(productId, orderItem.getQuantity());
            OrderItemsPojo orderItemPojo = new OrderItemsPojo(orderId, productId,
                    orderItem.getQuantity(), orderItem.getMrp());
            orderItemsToCreate.add(orderItemPojo);
        }
        return orderItemsToCreate;
    }
}