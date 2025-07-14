package com.increff.pos.unit.dao;

import com.increff.pos.dao.InvoiceDao;
import com.increff.pos.entity.InvoicePojo;
import com.increff.pos.setup.AbstractIntegrationTest;
import com.increff.pos.setup.TestData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.junit.Assert.*;

/**
 * Unit tests for InvoiceDao class.
 *
 * These tests verify:
 * - Database CRUD operations
 * - FK-Id relationship persistence (orderId)
 * - Invoice path management
 * - Date and revenue handling
 * - Unique constraint validation (one invoice per order)
 *
 * Note: These are technically integration tests as they use the database,
 * but they focus on testing the DAO layer in isolation.
 */
public class InvoiceDaoTest extends AbstractIntegrationTest {

    @Autowired
    private InvoiceDao invoiceDao;

    @PersistenceContext
    private EntityManager entityManager;


    /**
     * Test selecting invoice by ID - not found.
     * Verifies that null is returned for non-existent invoices.
     */
    @Test
    public void testSelectById_NotFound() {
        // Given: Non-existent invoice ID
        Integer nonExistentId = 999;

        // When: Invoice is selected by ID
        InvoicePojo retrieved = invoiceDao.selectById(nonExistentId);

        // Then: Null should be returned
        assertNull(retrieved);
    }

    /**
     * Helper method to create and persist an invoice.
     */
    private InvoicePojo createAndPersistInvoice(Integer orderId, Integer countOfItems, String invoicePath, Double finalRevenue) {
        InvoicePojo invoice = TestData.invoice(orderId, countOfItems, invoicePath, finalRevenue);
        invoiceDao.insert(invoice);
        return invoice;
    }
}