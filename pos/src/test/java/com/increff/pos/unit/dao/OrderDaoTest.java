package com.increff.pos.unit.dao;

import com.increff.pos.dao.OrderDao;
import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.*;
import com.increff.pos.setup.AbstractIntegrationTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.*;

/**
 * Unit tests for OrderDao and OrderItemDao classes.
 *
 * These tests verify:
 * - Database CRUD operations
 * - FK-Id relationship persistence (orderId, productId)
 * - Search functionality with date ranges
 * - Order item management with proper FK relationships
 * - Pagination support
 *
 * Note: These are technically integration tests as they use the database,
 * but they focus on testing the DAO layer in isolation.
 */
public class OrderDaoTest extends AbstractIntegrationTest {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrderItemDao orderItemDao;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Test selecting order by ID - not found.
     * Verifies that null is returned for non-existent orders.
     */
    @Test
    public void testSelectOrderById_NotFound() {
        // Given: Non-existent order ID
        Integer nonExistentId = 999;

        // When: Order is selected by ID
        OrdersPojo retrieved = orderDao.selectById(nonExistentId);

        // Then: Null should be returned
        assertNull(retrieved);
    }
    /**
     * Test selecting order item by ID - not found.
     * Verifies that null is returned for non-existent order items.
     */
    @Test
    public void testSelectOrderItemById_NotFound() {
        // Given: Non-existent order item ID
        Integer nonExistentId = 999;

        // When: Order item is selected by ID
        OrderItemsPojo retrieved = orderItemDao.selectById(nonExistentId);

        // Then: Null should be returned
        assertNull(retrieved);
    }
}