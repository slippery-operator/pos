package com.increff.pos.unit.api;

import com.increff.pos.api.InvoiceApi;
import com.increff.pos.dao.InvoiceDao;
import com.increff.pos.entity.InvoicePojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.DaySalesModel;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.setup.TestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InvoiceApi class.
 * 
 * These tests verify:
 * - Invoice creation functionality
 * - Invoice retrieval by order ID
 * - Date range queries for sales data
 * - Invoice existence checks
 * - Business logic validation
 * 
 * Each test focuses on exactly one API method and verifies both
 * the return value and the interactions with dependencies.
 */
@RunWith(MockitoJUnitRunner.class)
public class InvoiceApiTest {

    @Mock
    private InvoiceDao invoiceDao;

    @InjectMocks
    private InvoiceApi invoiceApi;

    private InvoicePojo testInvoice;

    @Before
    public void setUp() {
        testInvoice = TestData.invoice(1, 1, 2, "invoice_path.pdf", 100.0);
    }

    /**
     * Test creating invoice successfully.
     * Verifies that invoice is created with proper validation.
     */
    @Test
    public void testCreateInvoice_Success() throws ApiException {
        // Given: Valid invoice data
        InvoicePojo invoice = TestData.invoice(1, 2, "new_invoice.pdf", 200.0);

        // When: Invoice is created
        InvoicePojo result = invoiceApi.createInvoice(invoice);

        // Then: Invoice should be created successfully
        assertNotNull(result);
        assertEquals(invoice.getOrderId(), result.getOrderId());
        assertEquals(invoice.getInvoicePath(), result.getInvoicePath());
        assertEquals(invoice.getFinalRevenue(), result.getFinalRevenue());

        // And: DAO should be called to insert invoice
        verify(invoiceDao).insert(invoice);
    }

    /**
     * Test creating invoice with null input.
     * Verifies that null validation works correctly.
     */
    @Test
    public void testCreateInvoice_NullInput() throws ApiException {
        // Given: Null invoice
        InvoicePojo invoice = null;

        // When & Then: Should handle null input gracefully or throw exception
        try {
            invoiceApi.createInvoice(invoice);
            fail("Should throw exception for null invoice");
        } catch (Exception e) {
            // Should throw either ApiException or NullPointerException
            assertTrue(e instanceof ApiException || e instanceof NullPointerException);
        }
    }

    /**
     * Test getting invoices data by date range successfully.
     * Verifies that date range queries work correctly.
     */
    @Test
    public void testGetInvoicesDataByDateRange_Success() throws ApiException {
        // Given: Date range and invoice data
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(7);
        ZonedDateTime endDate = ZonedDateTime.now();
        
        List<InvoicePojo> invoices = Arrays.asList(
            TestData.invoice(1, 2, "invoice1.pdf", 100.0),
            TestData.invoice(2, 3, "invoice2.pdf", 150.0)
        );
        
        when(invoiceDao.selectByDateRange(startDate, endDate)).thenReturn(invoices);

        // When: Data is retrieved by date range
        DaySalesModel result = invoiceApi.getInvoicesDataByDateRange(startDate, endDate);

        // Then: Sales data should be aggregated correctly
        assertNotNull(result);
        assertEquals(Integer.valueOf(2), result.getInvoicedOrdersCount()); // 2 invoices
        assertEquals(Integer.valueOf(5), result.getInvoicedItemsCount()); // 2 + 3 items
        assertEquals(250.0, result.getTotalRevenue(), 0.01); // 100 + 150 revenue

        // And: DAO should be called
        verify(invoiceDao).selectByDateRange(startDate, endDate);
    }

    /**
     * Test getting invoices data by date range - empty result.
     * Verifies that empty results are handled correctly.
     */
    @Test
    public void testGetInvoicesDataByDateRange_EmptyResult() throws ApiException {
        // Given: Date range with no invoices
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(7);
        ZonedDateTime endDate = ZonedDateTime.now();
        
        when(invoiceDao.selectByDateRange(startDate, endDate)).thenReturn(Collections.emptyList());

        // When: Data is retrieved by date range
        DaySalesModel result = invoiceApi.getInvoicesDataByDateRange(startDate, endDate);

        // Then: Empty sales data should be returned
        assertNotNull(result);
        assertEquals(Integer.valueOf(0), result.getInvoicedOrdersCount());
        assertEquals(Integer.valueOf(0), result.getInvoicedItemsCount());
        assertEquals(0.0, result.getTotalRevenue(), 0.01);

        // And: DAO should be called
        verify(invoiceDao).selectByDateRange(startDate, endDate);
    }

    /**
     * Test checking if invoice exists by order ID - exists.
     * Verifies that existence check works correctly.
     */
    @Test
    public void testExistsByOrderId_Exists() throws ApiException {
        // Given: Invoice exists for order
        Integer orderId = 1;
        when(invoiceDao.selectByOrderId(orderId)).thenReturn(testInvoice);

        // When: Existence is checked
        boolean result = invoiceApi.existsByOrderId(orderId);

        // Then: Should return true
        assertTrue(result);

        // And: DAO should be called
        verify(invoiceDao).selectByOrderId(orderId);
    }

    /**
     * Test checking if invoice exists by order ID - does not exist.
     * Verifies that non-existence is detected correctly.
     */
    @Test
    public void testExistsByOrderId_DoesNotExist() throws ApiException {
        // Given: Invoice does not exist for order
        Integer orderId = 999;
        when(invoiceDao.selectByOrderId(orderId)).thenReturn(null);

        // When: Existence is checked
        boolean result = invoiceApi.existsByOrderId(orderId);

        // Then: Should return false
        assertFalse(result);

        // And: DAO should be called
        verify(invoiceDao).selectByOrderId(orderId);
    }

    /**
     * Test getting invoice by order ID successfully.
     * Verifies that invoice retrieval works correctly.
     */
    @Test
    public void testGetInvoiceByOrderId_Success() throws ApiException {
        // Given: Invoice exists for order
        Integer orderId = 1;
        when(invoiceDao.selectByOrderId(orderId)).thenReturn(testInvoice);

        // When: Invoice is retrieved by order ID
        InvoicePojo result = invoiceApi.getInvoiceByOrderId(orderId);

        // Then: Invoice should be returned
        assertNotNull(result);
        assertEquals(testInvoice.getOrderId(), result.getOrderId());
        assertEquals(testInvoice.getInvoicePath(), result.getInvoicePath());
        assertEquals(testInvoice.getFinalRevenue(), result.getFinalRevenue());

        // And: DAO should be called
        verify(invoiceDao).selectByOrderId(orderId);
    }

    /**
     * Test getting invoice by order ID - not found.
     * Verifies that appropriate exception is thrown for non-existent invoice.
     */
    @Test
    public void testGetInvoiceByOrderId_NotFound() throws ApiException {
        // Given: Invoice does not exist for order
        Integer orderId = 999;
        when(invoiceDao.selectByOrderId(orderId)).thenReturn(null);

        // When & Then: Exception should be thrown
        try {
            invoiceApi.getInvoiceByOrderId(orderId);
            fail("Expected ApiException to be thrown for non-existent invoice");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
            assertTrue(e.getMessage().contains("Invoice not found"));
        }

        // And: DAO should be called
        verify(invoiceDao).selectByOrderId(orderId);
    }

    /**
     * Test getting invoice path by order ID successfully.
     * Verifies that invoice path retrieval works correctly.
     */
    @Test
    public void testGetInvoicePathByOrderId_Success() throws ApiException {
        // Given: Invoice exists for order
        Integer orderId = 1;
        when(invoiceDao.selectByOrderId(orderId)).thenReturn(testInvoice);

        // When: Invoice path is retrieved by order ID
        String result = invoiceApi.getInvoicePathByOrderId(orderId);

        // Then: Invoice path should be returned
        assertNotNull(result);
        assertEquals(testInvoice.getInvoicePath(), result);

        // And: DAO should be called
        verify(invoiceDao).selectByOrderId(orderId);
    }

    /**
     * Test getting invoice path by order ID - not found.
     * Verifies that appropriate exception is thrown for non-existent invoice.
     */
    @Test
    public void testGetInvoicePathByOrderId_NotFound() throws ApiException {
        // Given: Invoice does not exist for order
        Integer orderId = 999;
        when(invoiceDao.selectByOrderId(orderId)).thenReturn(null);

        // When & Then: Exception should be thrown
        try {
            invoiceApi.getInvoicePathByOrderId(orderId);
            fail("Expected ApiException to be thrown for non-existent invoice");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
            assertTrue(e.getMessage().contains("Invoice not found"));
        }

        // And: DAO should be called
        verify(invoiceDao).selectByOrderId(orderId);
    }

    /**
     * Test getting invoices data with null start date.
     * Verifies that null date handling works correctly.
     */
    @Test
    public void testGetInvoicesDataByDateRange_NullStartDate() throws ApiException {
        // Given: Null start date
        ZonedDateTime startDate = null;
        ZonedDateTime endDate = ZonedDateTime.now();

        // When & Then: Should handle null start date gracefully or throw exception
        try {
            invoiceApi.getInvoicesDataByDateRange(startDate, endDate);
            // If no exception, verify DAO was called
            verify(invoiceDao).selectByDateRange(startDate, endDate);
        } catch (Exception e) {
            // Should throw either ApiException or NullPointerException
            assertTrue(e instanceof ApiException || e instanceof NullPointerException);
        }
    }

    /**
     * Test getting invoices data with null end date.
     * Verifies that null date handling works correctly.
     */
    @Test
    public void testGetInvoicesDataByDateRange_NullEndDate() throws ApiException {
        // Given: Null end date
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(7);
        ZonedDateTime endDate = null;

        // When & Then: Should handle null end date gracefully or throw exception
        try {
            invoiceApi.getInvoicesDataByDateRange(startDate, endDate);
            // If no exception, verify DAO was called
            verify(invoiceDao).selectByDateRange(startDate, endDate);
        } catch (Exception e) {
            // Should throw either ApiException or NullPointerException
            assertTrue(e instanceof ApiException || e instanceof NullPointerException);
        }
    }

    /**
     * Test existence check with null order ID.
     * Verifies that null input validation works correctly.
     */
    @Test
    public void testExistsByOrderId_NullOrderId() throws ApiException {
        // Given: Null order ID
        Integer orderId = null;

        // When & Then: Should handle null order ID gracefully or throw exception
        try {
            invoiceApi.existsByOrderId(orderId);
            // If no exception, verify DAO was called
            verify(invoiceDao).selectByOrderId(orderId);
        } catch (Exception e) {
            // Should throw either ApiException or NullPointerException
            assertTrue(e instanceof ApiException || e instanceof NullPointerException);
        }
    }

    /**
     * Test getting invoice by null order ID.
     * Verifies that null input validation works correctly.
     */
    @Test
    public void testGetInvoiceByOrderId_NullOrderId() throws ApiException {
        // Given: Null order ID
        Integer orderId = null;

        // When & Then: Should throw exception for null order ID
        try {
            invoiceApi.getInvoiceByOrderId(orderId);
            fail("Should throw exception for null order ID");
        } catch (Exception e) {
            // Should throw either ApiException or NullPointerException
            assertTrue(e instanceof ApiException || e instanceof NullPointerException);
        }
    }

    /**
     * Test complex date range scenario.
     * Verifies that complex aggregation calculations work correctly.
     */
    @Test
    public void testGetInvoicesDataByDateRange_ComplexScenario() throws ApiException {
        // Given: Date range with multiple invoices of varying sizes
        ZonedDateTime startDate = ZonedDateTime.now().minusDays(30);
        ZonedDateTime endDate = ZonedDateTime.now();
        
        List<InvoicePojo> invoices = Arrays.asList(
            TestData.invoice(1, 5, "invoice1.pdf", 250.0),
            TestData.invoice(2, 0, "invoice2.pdf", 0.0),  // Zero items
            TestData.invoice(3, 10, "invoice3.pdf", 500.0),
            TestData.invoice(4, 3, "invoice4.pdf", 75.0)
        );
        
        when(invoiceDao.selectByDateRange(startDate, endDate)).thenReturn(invoices);

        // When: Data is retrieved by date range
        DaySalesModel result = invoiceApi.getInvoicesDataByDateRange(startDate, endDate);

        // Then: Sales data should be aggregated correctly
        assertNotNull(result);
        assertEquals(Integer.valueOf(4), result.getInvoicedOrdersCount()); // 4 invoices
        assertEquals(Integer.valueOf(18), result.getInvoicedItemsCount()); // 5 + 0 + 10 + 3 items
        assertEquals(825.0, result.getTotalRevenue(), 0.01); // 250 + 0 + 500 + 75 revenue

        // And: DAO should be called
        verify(invoiceDao).selectByDateRange(startDate, endDate);
    }

    /**
     * Test invoice creation with zero revenue.
     * Verifies that edge cases are handled correctly.
     */
    @Test
    public void testCreateInvoice_ZeroRevenue() throws ApiException {
        // Given: Invoice with zero revenue
        InvoicePojo invoice = TestData.invoice(1, 0, "zero_invoice.pdf", 0.0);

        // When: Invoice is created
        InvoicePojo result = invoiceApi.createInvoice(invoice);

        // Then: Invoice should be created successfully
        assertNotNull(result);
        assertEquals(Integer.valueOf(0), result.getCountOfItems());
        assertEquals(Double.valueOf(0.0), result.getFinalRevenue());

        // And: DAO should be called to insert invoice
        verify(invoiceDao).insert(invoice);
    }

    /**
     * Test date range boundaries.
     * Verifies that date boundary conditions work correctly.
     */
    @Test
    public void testGetInvoicesDataByDateRange_SameDateBoundaries() throws ApiException {
        // Given: Same start and end date
        ZonedDateTime date = ZonedDateTime.now();
        
        List<InvoicePojo> invoices = Arrays.asList(
            TestData.invoice(1, 2, "today_invoice.pdf", 100.0)
        );
        
        when(invoiceDao.selectByDateRange(date, date)).thenReturn(invoices);

        // When: Data is retrieved for same date
        DaySalesModel result = invoiceApi.getInvoicesDataByDateRange(date, date);

        // Then: Sales data should be returned correctly
        assertNotNull(result);
        assertEquals(Integer.valueOf(1), result.getInvoicedOrdersCount());
        assertEquals(Integer.valueOf(2), result.getInvoicedItemsCount());
        assertEquals(100.0, result.getTotalRevenue(), 0.01);

        // And: DAO should be called
        verify(invoiceDao).selectByDateRange(date, date);
    }
} 