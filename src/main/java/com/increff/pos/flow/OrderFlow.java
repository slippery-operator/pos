package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.OrderItemPojo;
import com.increff.pos.entity.OrderPojo;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.EntityNotFoundException;
import com.increff.pos.model.response.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;
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

    @Transactional
    public OrderPojo createOrderFromBarcodes(List<String> barcodes, List<Integer> quantities, List<Double> mrps) {
        // Look up products by their barcodes
        Map<String, ProductPojo> productMap = productApi.findProductsByBarcodes(barcodes);

        // Validate that all barcodes were found
        for (String barcode : barcodes) {
            if (!productMap.containsKey(barcode)) {
                throw new EntityNotFoundException("Product with barcode " + barcode + " not found");
            }
        }

        // Validate inventory availability for each item
        for (int i = 0; i < barcodes.size(); i++) {
            ProductPojo product = productMap.get(barcodes.get(i));
            inventoryApi.validateInventoryAvailability(product.getId(), quantities.get(i));
        }

        // Create the main order record
        OrderPojo createdOrder = orderApi.createOrder();

        // Process each order item
        List<OrderItemPojo> orderItemsToCreate = new ArrayList<>();
        for (int i = 0; i < barcodes.size(); i++) {
            // Get the product for this barcode
            ProductPojo product = productMap.get(barcodes.get(i));

            // Reduce inventory for this product
            inventoryApi.reduceInventory(product.getId(), quantities.get(i));

            // Create order item pojo
            OrderItemPojo orderItem = new OrderItemPojo();
            orderItem.setOrderId(createdOrder.getId());
            orderItem.setProductId(product.getId());
            orderItem.setQuantity(quantities.get(i));
            orderItem.setSellingPrice(mrps.get(i));
            orderItemsToCreate.add(orderItem);
        }

        // Bulk create all order items
        orderItemApi.bulkCreateOrderItems(orderItemsToCreate);

        return createdOrder;
    }

    public List<OrderPojo> searchOrders(ZonedDateTime startDate, ZonedDateTime endDate, Integer orderId) {
        return orderApi.searchOrders(startDate, endDate, orderId);
    }

    public OrderPojo getOrderWithItems(Integer orderId) {
        return orderApi.getOrderById(orderId);
    }

    public List<OrderItemPojo> getOrderItemsByOrderId(Integer orderId) {
        return orderItemApi.getOrderItemsByOrderId(orderId);
    }
}