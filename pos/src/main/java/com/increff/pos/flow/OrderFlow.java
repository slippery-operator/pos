package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.OrderItemModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class OrderFlow {

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private OrderItemApi orderItemApi;

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InventoryApi inventoryApi;

    /**
     * Creates an order from a list of OrderItemModel objects.
     * This method provides better readability and reduces the risk of index mismatches
     * by using a structured model instead of separate lists.
     * 
     * @param orderItems List of OrderItemModel containing barcode, quantity, and MRP
     * @return Created OrdersPojo
     */
    @Transactional
    public OrdersPojo createOrder(List<OrderItemModel> orderItems) {
        // Extract barcodes from OrderItemModel for product lookup
        List<String> barcodes = orderItems.stream()
                .map(OrderItemModel::getBarcode)
                .collect(java.util.stream.Collectors.toList());

        // Look up products by their barcodes
        Map<String, Integer> productBarcodeToId = productApi.findProductsByBarcodes(barcodes);

        // Validate that all barcodes were found
        for (OrderItemModel orderItem : orderItems) {
            if (!productBarcodeToId.containsKey(orderItem.getBarcode())) {
                throw new ApiException(ApiException.ErrorType.NOT_FOUND, "Product not found");
            }
        }

        // Validate inventory availability for each item
        for (OrderItemModel orderItem : orderItems) {
            Integer productId = productBarcodeToId.get(orderItem.getBarcode());
            inventoryApi.validateInventoryAvailability(productId, orderItem.getQuantity());
        }

        // Create the main order record
        OrdersPojo createdOrder = orderApi.createOrder();

        // Process each order item using OrderItemModel for better readability
        List<OrderItemsPojo> orderItemsToCreate = new ArrayList<>();
        for (OrderItemModel orderItem : orderItems) {

            Integer productId = productBarcodeToId.get(orderItem.getBarcode());

            inventoryApi.reduceInventory(productId, orderItem.getQuantity());

            OrderItemsPojo orderItemPojo = new OrderItemsPojo(createdOrder.getId(), productId, orderItem.getQuantity(), orderItem.getMrp());
            orderItemsToCreate.add(orderItemPojo);
        }
        // Bulk create all order items
         orderItemApi.createOrderItemsGroup(orderItemsToCreate);

        return createdOrder;
    }
}