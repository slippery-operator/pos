package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.exception.EntityNotFoundException;
import com.increff.pos.model.form.OrderForm;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.model.response.OrderResponse;
import com.increff.pos.model.response.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Transactional
    public OrderResponse createOrderFromBarcodes(OrderForm orderForm) {
        // Extract all barcodes from the order items
        List<String> barcodes = orderForm.getOrderItems().stream()
                .map(OrderItemForm::getBarcode)
                .collect(Collectors.toList());

        // Look up products by their barcodes
        Map<String, ProductResponse> productMap = productApi.findProductsByBarcodes(barcodes);

        // Validate that all barcodes were found
        for (String barcode : barcodes) {
            if (!productMap.containsKey(barcode)) {
                throw new EntityNotFoundException("Product with barcode " + barcode + " not found");
            }
        }

        // Validate inventory availability for each item
        for (OrderItemForm orderItem : orderForm.getOrderItems()) {
            ProductResponse product = productMap.get(orderItem.getBarcode());
            inventoryApi.validateInventoryAvailability(product.getId(), orderItem.getQuantity());
        }

        // Create the main order record
        OrderResponse createdOrder = orderApi.createOrder();

        // Process each order item
        List<OrderItemForm> orderItemsToCreate = new ArrayList<>();
        for (OrderItemForm orderItem : orderForm.getOrderItems()) {
            // Get the product for this barcode
            ProductResponse product = productMap.get(orderItem.getBarcode());

            // Reduce inventory for this product
            inventoryApi.reduceInventory(product.getId(), orderItem.getQuantity());

            // Prepare order item for creation
            orderItem.setProductId(product.getId());
            orderItem.setOrderId(createdOrder.getId());
            orderItemsToCreate.add(orderItem);
        }

        // Bulk create all order items
        orderItemApi.bulkCreateOrderItems(orderItemsToCreate);

        // Return the created order with items
        OrderResponse finalOrder = orderApi.getOrderById(createdOrder.getId());
        finalOrder.setOrderItems(orderItemApi.getOrderItemsByOrderId(createdOrder.getId()));

        return finalOrder;
    }

    public List<OrderResponse> searchOrders(java.time.ZonedDateTime startDate,
                                            java.time.ZonedDateTime endDate,
                                            Integer orderId) {
        return orderApi.searchOrders(startDate, endDate, orderId);
    }

    public OrderResponse getOrderWithItems(Integer orderId) {
        OrderResponse order = orderApi.getOrderById(orderId);

        // Get order items with product details
        List<com.increff.pos.model.response.OrderItemResponse> orderItems =
                orderItemApi.getOrderItemsByOrderId(orderId);

        // Enhance order items with product information
        for (com.increff.pos.model.response.OrderItemResponse item : orderItems) {
            try {
                ProductResponse product = productApi.getProductById(item.getProductId());
                item.setProductName(product.getName());
                item.setBarcode(product.getBarcode());
            } catch (EntityNotFoundException e) {
                item.setProductName("Product Not Found");
                item.setBarcode("N/A");
            }
        }

        order.setOrderItems(orderItems);
        return order;
    }
}