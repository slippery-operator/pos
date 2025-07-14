package com.increff.pos.setup;

import com.increff.pos.entity.*;
import com.increff.pos.model.enums.Role;
import java.time.ZonedDateTime;
import java.time.ZoneOffset;

/**
 * Centralized factory for lightweight test objects for POS system.
 * Add methods here instead of duplicating builders across test classes.
 * 
 * This class provides factory methods for creating test entities with proper
 * FK-Id relationships (using setProductId() instead of setProduct()).
 * 
 * Usage examples:
 * - TestData.client("Test Client")
 * - TestData.product("BC123", clientId, "Product Name", 100.0)
 * - TestData.inventory(productId, 50)
 * - TestData.orderItem(orderId, productId, 2, 25.0)
 */
public final class TestData {
    private TestData() {} // utility class – no instances

    /* ---- Clients ---- */
    
    /**
     * Creates a client with specified ID and name.
     * @param id Client ID (can be null for new entities)
     * @param name Client name
     * @return ClientPojo instance
     */
    public static ClientPojo client(Integer id, String name) {
        ClientPojo client = new ClientPojo();
        client.setClientId(id);
        client.setName(name);
        return client;
    }

    /**
     * Creates a client with only name (ID will be auto-generated).
     * @param name Client name
     * @return ClientPojo instance
     */
    public static ClientPojo client(String name) {
        return client(null, name);
    }

    /* ---- Products ---- */
    
    /**
     * Creates a product with all fields specified.
     * @param id Product ID (can be null for new entities)
     * @param barcode Product barcode (must be unique)
     * @param clientId Foreign key to client
     * @param name Product name
     * @param mrp Maximum retail price
     * @return ProductPojo instance
     */
    public static ProductPojo product(Integer id, String barcode, Integer clientId, String name, Double mrp) {
        ProductPojo product = new ProductPojo();
        product.setId(id);
        product.setBarcode(barcode);
        product.setClientId(clientId);
        product.setName(name);
        product.setMrp(mrp);
        return product;
    }

    /**
     * Creates a product without ID (for new entities).
     * @param barcode Product barcode (must be unique)
     * @param clientId Foreign key to client
     * @param name Product name
     * @param mrp Maximum retail price
     * @return ProductPojo instance
     */
    public static ProductPojo product(String barcode, Integer clientId, String name, Double mrp) {
        return product(null, barcode, clientId, name, mrp);
    }

    /**
     * Creates a product with image URL.
     * @param barcode Product barcode
     * @param clientId Foreign key to client
     * @param name Product name
     * @param mrp Maximum retail price
     * @param imageUrl Product image URL
     * @return ProductPojo instance
     */
    public static ProductPojo productWithImage(String barcode, Integer clientId, String name, Double mrp, String imageUrl) {
        ProductPojo product = product(barcode, clientId, name, mrp);
        product.setImageUrl(imageUrl);
        return product;
    }

    /* ---- Inventory ---- */
    
    /**
     * Creates inventory with specified ID, product ID, and quantity.
     * @param id Inventory ID (can be null for new entities)
     * @param productId Foreign key to product
     * @param quantity Stock quantity
     * @return InventoryPojo instance
     */
    public static InventoryPojo inventory(Integer id, Integer productId, Integer quantity) {
        InventoryPojo inventory = new InventoryPojo();
        inventory.setId(id);
        inventory.setProductId(productId);
        inventory.setQuantity(quantity);
        return inventory;
    }

    /**
     * Creates inventory without ID (for new entities).
     * @param productId Foreign key to product
     * @param quantity Stock quantity
     * @return InventoryPojo instance
     */
    public static InventoryPojo inventory(Integer productId, Integer quantity) {
        return inventory(null, productId, quantity);
    }

    /* ---- Orders ---- */
    
    /**
     * Creates an order with specified ID.
     * @param id Order ID (can be null for new entities)
     * @return OrdersPojo instance
     */
    public static OrdersPojo order(Integer id) {
        OrdersPojo order = new OrdersPojo();
        order.setId(id);
        return order;
    }

    /**
     * Creates an order without ID (for new entities).
     * @return OrdersPojo instance
     */
    public static OrdersPojo order() {
        return order(null);
    }

    /* ---- Order Items ---- */
    
    /**
     * Creates an order item with all fields specified.
     * @param id Order item ID (can be null for new entities)
     * @param orderId Foreign key to order
     * @param productId Foreign key to product
     * @param quantity Item quantity
     * @param sellingPrice Selling price per unit
     * @return OrderItemsPojo instance
     */
    public static OrderItemsPojo orderItem(Integer id, Integer orderId, Integer productId, Integer quantity, Double sellingPrice) {
        OrderItemsPojo orderItem = new OrderItemsPojo();
        orderItem.setId(id);
        orderItem.setOrderId(orderId);
        orderItem.setProductId(productId);
        orderItem.setQuantity(quantity);
        orderItem.setSellingPrice(sellingPrice);
        return orderItem;
    }

    /**
     * Creates an order item without ID (for new entities).
     * @param orderId Foreign key to order
     * @param productId Foreign key to product
     * @param quantity Item quantity
     * @param sellingPrice Selling price per unit
     * @return OrderItemsPojo instance
     */
    public static OrderItemsPojo orderItem(Integer orderId, Integer productId, Integer quantity, Double sellingPrice) {
        return orderItem(null, orderId, productId, quantity, sellingPrice);
    }

    /* ---- Invoices ---- */
    
    /**
     * Creates an invoice with all fields specified.
     * @param id Invoice ID (can be null for new entities)
     * @param orderId Foreign key to order
     * @param countOfItems Total number of items in invoice
     * @param invoicePath File path to generated invoice PDF
     * @param finalRevenue Total revenue from invoice
     * @return InvoicePojo instance
     */
    public static InvoicePojo invoice(Integer id, Integer orderId, Integer countOfItems, String invoicePath, Double finalRevenue) {
        InvoicePojo invoice = new InvoicePojo();
        invoice.setId(id);
        invoice.setOrderId(orderId);
        invoice.setTimeStamp(ZonedDateTime.now(ZoneOffset.UTC));
        invoice.setCountOfItems(countOfItems);
        invoice.setInvoicePath(invoicePath);
        invoice.setFinalRevenue(finalRevenue);
        return invoice;
    }

    /**
     * Creates an invoice without ID (for new entities).
     * @param orderId Foreign key to order
     * @param countOfItems Total number of items in invoice
     * @param invoicePath File path to generated invoice PDF
     * @param finalRevenue Total revenue from invoice
     * @return InvoicePojo instance
     */
    public static InvoicePojo invoice(Integer orderId, Integer countOfItems, String invoicePath, Double finalRevenue) {
        return invoice(null, orderId, countOfItems, invoicePath, finalRevenue);
    }

    /**
     * Creates an invoice with custom timestamp.
     * @param orderId Foreign key to order
     * @param timestamp Custom timestamp
     * @param countOfItems Total number of items in invoice
     * @param invoicePath File path to generated invoice PDF
     * @param finalRevenue Total revenue from invoice
     * @return InvoicePojo instance
     */
    public static InvoicePojo invoiceWithTimestamp(Integer orderId, ZonedDateTime timestamp, Integer countOfItems, String invoicePath, Double finalRevenue) {
        InvoicePojo invoice = invoice(orderId, countOfItems, invoicePath, finalRevenue);
        invoice.setTimeStamp(timestamp);
        return invoice;
    }

    /* ---- Users ---- */
    
    /**
     * Creates a user with all fields specified.
     * @param id User ID (can be null for new entities)
     * @param email User email (must be unique)
     * @param name User display name
     * @param password User password
     * @param role User role (ADMIN, OPERATOR, etc.)
     * @return UserPojo instance
     */
    public static UserPojo user(Integer id, String email, String name, String password, Role role) {
        UserPojo user = new UserPojo();
        user.setId(id);
        user.setEmail(email);
        user.setName(name);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }

    /**
     * Creates a user without ID (for new entities).
     * @param email User email (must be unique)
     * @param name User display name
     * @param password User password
     * @param role User role (ADMIN, OPERATOR, etc.)
     * @return UserPojo instance
     */
    public static UserPojo user(String email, String name, String password, Role role) {
        return user(null, email, name, password, role);
    }

    /**
     * Creates an admin user for testing.
     * @param email Admin email
     * @param name Admin name
     * @param password Admin password
     * @return UserPojo instance with ADMIN role
     */
    public static UserPojo adminUser(String email, String name, String password) {
        return user(email, name, password, Role.SUPERVISOR);
    }

    /**
     * Creates an operator user for testing.
     * @param email Operator email
     * @param name Operator name
     * @param password Operator password
     * @return UserPojo instance with OPERATOR role
     */
    public static UserPojo operatorUser(String email, String name, String password) {
        return user(email, name, password, Role.OPERATOR);
    }

    /* ---- Day Sales ---- */
    
    /**
     * Creates day sales record with all fields specified.
     * @param id Day sales ID (can be null for new entities)
     * @param date Date of sales
     * @param invoicedOrdersCount Number of invoiced orders
     * @param invoicedItemsCount Number of invoiced items
     * @param totalRevenue Total revenue for the day
     * @return DaySalesPojo instance
     */
    public static DaySalesPojo daySales(Integer id, ZonedDateTime date, Integer invoicedOrdersCount, Integer invoicedItemsCount, Double totalRevenue) {
        DaySalesPojo daySales = new DaySalesPojo();
        daySales.setId(id);
        daySales.setDate(date);
        daySales.setInvoicedOrdersCount(invoicedOrdersCount);
        daySales.setInvoicedItemsCount(invoicedItemsCount);
        daySales.setTotalRevenue(totalRevenue);
        return daySales;
    }

    /**
     * Creates day sales record without ID (for new entities).
     * @param date Date of sales
     * @param invoicedOrdersCount Number of invoiced orders
     * @param invoicedItemsCount Number of invoiced items
     * @param totalRevenue Total revenue for the day
     * @return DaySalesPojo instance
     */
    public static DaySalesPojo daySales(ZonedDateTime date, Integer invoicedOrdersCount, Integer invoicedItemsCount, Double totalRevenue) {
        return daySales(null, date, invoicedOrdersCount, invoicedItemsCount, totalRevenue);
    }

    /**
     * Creates day sales record for today.
     * @param invoicedOrdersCount Number of invoiced orders
     * @param invoicedItemsCount Number of invoiced items
     * @param totalRevenue Total revenue for the day
     * @return DaySalesPojo instance
     */
    public static DaySalesPojo todaySales(Integer invoicedOrdersCount, Integer invoicedItemsCount, Double totalRevenue) {
        return daySales(ZonedDateTime.now(ZoneOffset.UTC), invoicedOrdersCount, invoicedItemsCount, totalRevenue);
    }

    // Cursor will automatically append new factory methods
    // when unfamiliar entities appear in rewritten tests
} 