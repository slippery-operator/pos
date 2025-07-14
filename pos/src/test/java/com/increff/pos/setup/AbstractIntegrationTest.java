package com.increff.pos.setup;

import com.increff.pos.dao.*;
import com.increff.pos.entity.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.AnnotationConfigWebContextLoader;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

/**
 * Base class for all integration tests in POS system.
 * Provides common setup and DAO access for database operations.
 * 
 * This class is designed for integration tests that:
 * - Test complete workflows across multiple layers
 * - Require database persistence and retrieval
 * - Verify end-to-end functionality
 * 
 * All integration tests should extend this class to get:
 * - Proper Spring context configuration
 * - Database transaction management (auto-rollback)
 * - Access to all DAO objects for data verification
 * - Consistent test environment setup
 * 
 * Usage Guidelines:
 * - Use TestData factory methods to create test entities
 * - Use DAO methods for data persistence instead of calling other APIs
 * - Verify results from both DTO return values AND database state
 * - Each test should focus on exactly one DTO method
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = QaConfig.class, loader = AnnotationConfigWebContextLoader.class)
@WebAppConfiguration("src/test/webapp")
@Transactional  // Ensures all database changes are rolled back after each test
public abstract class AbstractIntegrationTest {

    // DAO dependencies for direct database access and verification
    
    @Autowired
    protected ProductDao productDao;

    @Autowired
    protected ClientDao clientDao;

    @Autowired
    protected InventoryDao inventoryDao;

    @Autowired
    protected OrderDao orderDao;

    @Autowired
    protected OrderItemDao orderItemDao;

    @Autowired
    protected InvoiceDao invoiceDao;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected ReportDao reportDao;

    /**
     * Helper method to create and persist a test client.
     * @param name Client name
     * @return Persisted ClientPojo with generated ID
     */
    protected ClientPojo createAndPersistClient(String name) {
        ClientPojo client = TestData.client(name);
        clientDao.insert(client);
        return client;
    }

    /**
     * Helper method to create and persist a test product.
     * @param barcode Product barcode
     * @param clientId Client ID (foreign key)
     * @param name Product name
     * @param mrp Maximum retail price
     * @return Persisted ProductPojo with generated ID
     */
    protected ProductPojo createAndPersistProduct(String barcode, Integer clientId, String name, Double mrp) {
        ProductPojo product = TestData.product(barcode, clientId, name, mrp);
        productDao.insert(product);
        return product;
    }

    /**
     * Helper method to create and persist inventory.
     * @param productId Product ID (foreign key)
     * @param quantity Stock quantity
     * @return Persisted InventoryPojo with generated ID
     */
    protected InventoryPojo createAndPersistInventory(Integer productId, Integer quantity) {
        InventoryPojo inventory = TestData.inventory(productId, quantity);
        inventoryDao.insert(inventory);
        return inventory;
    }

    /**
     * Helper method to create and persist an order.
     * @return Persisted OrdersPojo with generated ID
     */
    protected OrdersPojo createAndPersistOrder() {
        OrdersPojo order = TestData.order();
        orderDao.insert(order);
        return order;
    }

    /**
     * Helper method to create a complete order with all dependencies.
     * Creates client, product, inventory, order, and order items.
     * @return Persisted OrdersPojo with generated ID and associated order items
     */
    protected OrdersPojo createCompleteOrderWithDependencies() {
        // Create client
        ClientPojo client = createAndPersistClient("Test Client");
        
        // Create product
        ProductPojo product = createAndPersistProduct("TEST123", client.getClientId(), "Test Product", 100.0);
        
        // Create inventory
        createAndPersistInventory(product.getId(), 50);
        
        // Create order
        OrdersPojo order = createAndPersistOrder();
        
        // Create order item
        createAndPersistOrderItem(order.getId(), product.getId(), 2, 25.0);
        
        return order;
    }

    /**
     * Helper method to create and persist an order item.
     * @param orderId Order ID (foreign key)
     * @param productId Product ID (foreign key)
     * @param quantity Item quantity
     * @param sellingPrice Selling price per unit
     * @return Persisted OrderItemsPojo with generated ID
     */
    protected OrderItemsPojo createAndPersistOrderItem(Integer orderId, Integer productId, Integer quantity, Double sellingPrice) {
        OrderItemsPojo orderItem = TestData.orderItem(orderId, productId, quantity, sellingPrice);
        orderItemDao.insert(orderItem);
        return orderItem;
    }

    /**
     * Helper method to create and persist a user.
     * @param email User email
     * @param name User name
     * @param password User password
     * @param role User role
     * @return Persisted UserPojo with generated ID
     */
    protected UserPojo createAndPersistUser(String email, String name, String password, com.increff.pos.model.enums.Role role) {
        UserPojo user = TestData.user(email, name, password, role);
        userDao.insert(user);
        return user;
    }

    // Additional helper methods can be added here as needed
    // for common test setup operations
} 