package com.increff.pos.unit.api;

import com.increff.pos.api.OrderApi;
import com.increff.pos.dao.OrderDao;
import com.increff.pos.dao.OrderItemDao;
import com.increff.pos.entity.OrdersPojo;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.setup.TestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderApi class.
 * 
 * These tests focus on:
 * - Business logic validation
 * - Error handling
 * - Interaction with DAO layer
 * - Proper use of FK-Id relationships
 * - Order search functionality
 * 
 * All dependencies are mocked to ensure isolation and fast execution.
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderApiTest {

    @Mock
    private OrderDao orderDao;

    @Mock
    private OrderItemDao orderItemDao;

    @InjectMocks
    private OrderApi orderApi;

    private OrdersPojo testOrder;
    private OrderItemsPojo testOrderItem;

    @Before
    public void setUp() {
        // Setup test data using TestData factory with proper FK-Id relationships
        testOrder = TestData.order(1);
        testOrderItem = TestData.orderItem(1, 1, 1, 2, 25.0); // orderId=1, productId=1
    }

    /**
     * Test searching orders with all parameters.
     * Verifies that search parameters are passed correctly to DAO.
     */

    @Test
    public void testSearchOrders_WithAllParameters() {
        // Given: Search parameters
        ZonedDateTime startDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(7);
        ZonedDateTime endDate = ZonedDateTime.now(ZoneOffset.UTC);
        Integer orderId = 1;
        int page = 0;
        int size = 10;
        
        List<OrdersPojo> expectedOrders = Arrays.asList(
            TestData.order(1),
            TestData.order(2)
        );

        // When: DAO returns matching orders
        when(orderDao.findBySearchCriteria(startDate, endDate, orderId, page, size)).thenReturn(expectedOrders);

        // Then: API should return the same orders
        List<OrdersPojo> result = orderApi.searchOrders(startDate, endDate, orderId, page, size);

        assertEquals(2, result.size());
        assertEquals(expectedOrders, result);
        verify(orderDao).findBySearchCriteria(startDate, endDate, orderId, page, size);
    }

    /**
     * Test searching orders with null parameters.
     * Verifies that null parameters are handled correctly.
     */
    @Test
    public void testSearchOrders_WithNullParameters() {
        // Given: Search parameters with nulls
        ZonedDateTime startDate = null;
        ZonedDateTime endDate = null;
        Integer orderId = null;
        int page = 0;
        int size = 10;
        
        List<OrdersPojo> expectedOrders = Arrays.asList(
            TestData.order(1),
            TestData.order(2),
            TestData.order(3)
        );

        // When: DAO returns all orders
        when(orderDao.findBySearchCriteria(startDate, endDate, orderId, page, size)).thenReturn(expectedOrders);

        // Then: API should return all orders
        List<OrdersPojo> result = orderApi.searchOrders(startDate, endDate, orderId, page, size);

        assertEquals(3, result.size());
        assertEquals(expectedOrders, result);
        verify(orderDao).findBySearchCriteria(startDate, endDate, orderId, page, size);
    }

    /**
     * Test searching orders - no results.
     * Verifies that empty result is handled correctly.
     */
    // TODO: add more testcases involving only orderId, start date > end date
    @Test
    public void testSearchOrders_NoResults() {
        // Given: Search parameters that return no results
        ZonedDateTime startDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1);
        ZonedDateTime endDate = ZonedDateTime.now(ZoneOffset.UTC);
        Integer orderId = 999;
        int page = 0;
        int size = 10;

        // When: DAO returns empty list
        when(orderDao.findBySearchCriteria(startDate, endDate, orderId, page, size)).thenReturn(Collections.emptyList());

        // Then: API should return empty list
        List<OrdersPojo> result = orderApi.searchOrders(startDate, endDate, orderId, page, size);

        assertEquals(0, result.size());
        verify(orderDao).findBySearchCriteria(startDate, endDate, orderId, page, size);
    }

    /**
     * Test getting order by ID successfully.
     * Verifies that order is retrieved correctly.
     */
    @Test
    public void testGetOrderById_Success() {
        // Given: Order exists
        Integer orderId = 1;
        when(orderDao.selectById(orderId)).thenReturn(testOrder);

        // When: API retrieves order
        OrdersPojo result = orderApi.getOrderById(orderId);

        // Then: Order should be returned
        assertEquals(testOrder, result);
        assertEquals(orderId, result.getId());
        verify(orderDao).selectById(orderId);
    }

    /**
     * Test getting order by ID - not found.
     * Verifies that appropriate exception is thrown when order doesn't exist.
     */
    @Test
    public void testGetOrderById_NotFound() {
        // Given: Order doesn't exist
        Integer orderId = 999;
        when(orderDao.selectById(orderId)).thenReturn(null);

        // When & Then: Exception should be thrown
        try {
            orderApi.getOrderById(orderId);
            fail("Expected ApiException to be thrown for non-existent order");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
//            assertTrue(e.getMessage().contains("Order not found"));
        }

        verify(orderDao).selectById(orderId);
    }

    /**
     * Test creating order successfully.
     * Verifies that order is created and persisted.
     */
    @Test
    public void testCreateOrder_Success() {
        // Given: No existing order
        OrdersPojo newOrder = TestData.order();

        // When: API creates order
        OrdersPojo result = orderApi.createOrder();

        // Then: Order should be created
        assertNotNull(result);
        verify(orderDao).insert(any(OrdersPojo.class));
    }

    /**
     * Test updating invoice path successfully.
     * Verifies that order's invoice path is updated correctly.
     */
    @Test
    public void testUpdateInvoicePath_Success() {
        // Given: Order exists
        Integer orderId = 1;
        String invoicePath = "/path/to/invoice.pdf";
        when(orderDao.selectById(orderId)).thenReturn(testOrder);

        // When: API updates invoice path
        orderApi.updateInvoicePath(orderId, invoicePath);

        // Then: Order's invoice path should be updated
        verify(orderDao).selectById(orderId);
        verify(orderDao).updateInvoicePath(orderId, invoicePath);
    }

    /**
     * Test updating invoice path - order not found.
     * Verifies that appropriate exception is thrown when order doesn't exist.
     */
    @Test
    public void testUpdateInvoicePath_OrderNotFound() {
        // Given: Order doesn't exist
        Integer orderId = 999;
        String invoicePath = "/path/to/invoice.pdf";
        when(orderDao.selectById(orderId)).thenReturn(null);

        // When & Then: Exception should be thrown
        try {
            orderApi.updateInvoicePath(orderId, invoicePath);
            fail("Expected ApiException to be thrown");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
        }

        verify(orderDao).selectById(orderId);
        verify(orderDao, never()).updateInvoicePath(any(), any());
    }

    /**
     * Test updating invoice path with null path.
     * Verifies that null invoice path is handled correctly.
     */
    @Test
    public void testUpdateInvoicePath_NullPath() {
        // Given: Order exists and invoice path is null
        Integer orderId = 1;
        String invoicePath = null;
        when(orderDao.selectById(orderId)).thenReturn(testOrder);

        // When: API updates invoice path with null
        orderApi.updateInvoicePath(orderId, invoicePath);

        // Then: Order should be updated with null path
        verify(orderDao).selectById(orderId);
        verify(orderDao).updateInvoicePath(orderId, invoicePath);
    }

    /**
     * Test updating invoice path with empty path.
     * Verifies that empty invoice path is handled correctly.
     */
    @Test
    public void testUpdateInvoicePath_EmptyPath() {
        // Given: Order exists and invoice path is empty
        Integer orderId = 1;
        String invoicePath = "";
        when(orderDao.selectById(orderId)).thenReturn(testOrder);

        // When: API updates invoice path with empty string
        orderApi.updateInvoicePath(orderId, invoicePath);

        // Then: Order should be updated with empty path
        verify(orderDao).selectById(orderId);
        verify(orderDao).updateInvoicePath(orderId, invoicePath);
    }

    /**
     * Test searching orders with date range.
     * Verifies that date range filtering works correctly.
     */
    @Test
    public void testSearchOrders_WithDateRange() {
        // Given: Date range parameters
        ZonedDateTime startDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(30);
        ZonedDateTime endDate = ZonedDateTime.now(ZoneOffset.UTC);
        Integer orderId = null;
        int page = 0;
        int size = 20;
        
        List<OrdersPojo> expectedOrders = Arrays.asList(
            TestData.order(1),
            TestData.order(2),
            TestData.order(3),
            TestData.order(4)
        );

        // When: DAO returns orders within date range
        when(orderDao.findBySearchCriteria(startDate, endDate, orderId, page, size)).thenReturn(expectedOrders);

        // Then: API should return orders within date range
        List<OrdersPojo> result = orderApi.searchOrders(startDate, endDate, orderId, page, size);

        assertEquals(4, result.size());
        assertEquals(expectedOrders, result);
        verify(orderDao).findBySearchCriteria(startDate, endDate, orderId, page, size);
    }

    /**
     * Test searching orders with pagination.
     * Verifies that pagination parameters are handled correctly.
     */
    @Test
    public void testSearchOrders_WithPagination() {
        // Given: Pagination parameters
        ZonedDateTime startDate = null;
        ZonedDateTime endDate = null;
        Integer orderId = null;
        int page = 1;
        int size = 5;
        
        List<OrdersPojo> expectedOrders = Arrays.asList(
            TestData.order(6),
            TestData.order(7),
            TestData.order(8),
            TestData.order(9),
            TestData.order(10)
        );

        // When: DAO returns second page of orders
        when(orderDao.findBySearchCriteria(startDate, endDate, orderId, page, size)).thenReturn(expectedOrders);

        // Then: API should return second page
        List<OrdersPojo> result = orderApi.searchOrders(startDate, endDate, orderId, page, size);

        assertEquals(5, result.size());
        assertEquals(expectedOrders, result);
        verify(orderDao).findBySearchCriteria(startDate, endDate, orderId, page, size);
    }

    /**
     * Test edge case - searching with future date range.
     * Verifies that future date ranges are handled correctly.
     */
    @Test
    public void testSearchOrders_FutureDateRange() {
        // Given: Future date range
        ZonedDateTime startDate = ZonedDateTime.now(ZoneOffset.UTC).plusDays(1);
        ZonedDateTime endDate = ZonedDateTime.now(ZoneOffset.UTC).plusDays(7);
        Integer orderId = null;
        int page = 0;
        int size = 10;

        // When: DAO returns empty list for future dates
        when(orderDao.findBySearchCriteria(startDate, endDate, orderId, page, size)).thenReturn(Collections.emptyList());

        // Then: API should return empty list
        List<OrdersPojo> result = orderApi.searchOrders(startDate, endDate, orderId, page, size);

        assertEquals(0, result.size());
        verify(orderDao).findBySearchCriteria(startDate, endDate, orderId, page, size);
    }

    /**
     * Test edge case - searching with inverted date range.
     * Verifies that inverted date ranges (end before start) are handled.
     */
    @Test
    public void testSearchOrders_InvertedDateRange() {
        // Given: Inverted date range (end before start)
        ZonedDateTime startDate = ZonedDateTime.now(ZoneOffset.UTC);
        ZonedDateTime endDate = ZonedDateTime.now(ZoneOffset.UTC).minusDays(7);
        Integer orderId = null;
        int page = 0;
        int size = 10;

        // When: DAO handles inverted date range
        when(orderDao.findBySearchCriteria(startDate, endDate, orderId, page, size)).thenReturn(Collections.emptyList());

        // Then: API should return empty list
        List<OrdersPojo> result = orderApi.searchOrders(startDate, endDate, orderId, page, size);

        assertEquals(0, result.size());
        verify(orderDao).findBySearchCriteria(startDate, endDate, orderId, page, size);
    }
} 