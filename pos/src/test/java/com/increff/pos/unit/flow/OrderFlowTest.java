package com.increff.pos.unit.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.OrderItemApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.*;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.OrderFlow;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.form.OrderItemForm;
import com.increff.pos.setup.TestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderFlow class.
 * 
 * These tests verify:
 * - Order creation workflow
 * - Product barcode validation
 * - Inventory availability checks
 * - API interaction patterns
 * - Error handling scenarios
 */
@RunWith(MockitoJUnitRunner.class)
public class OrderFlowTest {

    @Mock
    private OrderApi orderApi;

    @Mock
    private OrderItemApi orderItemApi;

    @Mock
    private ProductApi productApi;

    @Mock
    private InventoryApi inventoryApi;

    @InjectMocks
    private OrderFlow orderFlow;

    private ClientPojo testClient;
    private ProductPojo testProduct1;
    private ProductPojo testProduct2;
    private OrdersPojo testOrder;

    @Before
    public void setUp() {
        // Set up test data
        testClient = TestData.client(1, "Test Client");
        testProduct1 = TestData.product(1, "FLOW001", testClient.getClientId(), "Flow Product 1", 100.0);
        testProduct2 = TestData.product(2, "FLOW002", testClient.getClientId(), "Flow Product 2", 150.0);
        testOrder = TestData.order(1);
    }

    /**
     * Test creating order successfully.
     * Verifies that the complete order creation workflow works correctly.
     */
    @Test
    public void testCreateOrder_Success() {
        // Given: Valid order items with barcodes
        OrderItemForm item1 = new OrderItemForm();
        item1.setBarcode(testProduct1.getBarcode());
        item1.setQuantity(5);
        item1.setMrp(90.0);

        OrderItemForm item2 = new OrderItemForm();
        item2.setBarcode(testProduct2.getBarcode());
        item2.setQuantity(3);
        item2.setMrp(140.0);

        List<OrderItemForm> orderItems = Arrays.asList(item1, item2);

        // And: Mock API responses
        Map<String, Integer> barcodeToIdMap = new HashMap<>();
        barcodeToIdMap.put(testProduct1.getBarcode(), testProduct1.getId());
        barcodeToIdMap.put(testProduct2.getBarcode(), testProduct2.getId());
        when(productApi.findProductsByBarcodes(any())).thenReturn(barcodeToIdMap);
        when(orderApi.createOrder()).thenReturn(testOrder);

        // When: OrderFlow creates the order
        OrdersPojo result = orderFlow.createOrder(orderItems);

        // Then: Result should be valid
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());

        // And: Verify API interactions
        verify(productApi, times(1)).findProductsByBarcodes(any());
        verify(inventoryApi, times(2)).validateInventoryAvailability(any(Integer.class), any(Integer.class));
        verify(orderApi, times(1)).createOrder();
        verify(orderItemApi, times(1)).createOrderItemsGroup(any());
        verify(inventoryApi, times(2)).reduceInventory(any(Integer.class), any(Integer.class));
    }

    /**
     * Test creating order with non-existent product.
     * Verifies that proper validation occurs for product existence.
     */
    @Test
    public void testCreateOrder_NonExistentProduct() {
        // Given: Order item with non-existent barcode
        OrderItemForm item = new OrderItemForm();
        item.setBarcode("NONEXISTENT");
        item.setQuantity(5);
        item.setMrp(90.0);

        List<OrderItemForm> orderItems = Arrays.asList(item);

        // And: Mock API to return empty map (product not found)
        Map<String, Integer> emptyMap = new HashMap<>();
        when(productApi.findProductsByBarcodes(any())).thenReturn(emptyMap);

        // When & Then: OrderFlow should throw exception
        try {
            orderFlow.createOrder(orderItems);
            fail("Should throw ApiException for non-existent product");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
            assertTrue(e.getMessage().contains("not found"));
        }

        // And: Verify API interactions
        verify(productApi, times(1)).findProductsByBarcodes(any());
        verify(inventoryApi, never()).validateInventoryAvailability(any(), any());
        verify(orderApi, never()).createOrder();
    }

    /**
     * Test creating order with insufficient inventory.
     * Verifies that inventory validation works correctly.
     */
    @Test
    public void testCreateOrder_InsufficientInventory() {
        // Given: Valid order item
        OrderItemForm item = new OrderItemForm();
        item.setBarcode(testProduct1.getBarcode());
        item.setQuantity(100); // More than available
        item.setMrp(90.0);

        List<OrderItemForm> orderItems = Arrays.asList(item);

        // And: Mock API responses
        Map<String, Integer> barcodeToIdMap = new HashMap<>();
        barcodeToIdMap.put(testProduct1.getBarcode(), testProduct1.getId());
        when(productApi.findProductsByBarcodes(any())).thenReturn(barcodeToIdMap);
        
        // Mock inventory validation to throw exception
        doThrow(new ApiException(ErrorType.BAD_REQUEST, "Insufficient inventory"))
            .when(inventoryApi).validateInventoryAvailability(testProduct1.getId(), 100);

        // When & Then: OrderFlow should throw exception
        try {
            orderFlow.createOrder(orderItems);
            fail("Should throw ApiException for insufficient inventory");
        } catch (ApiException e) {
            assertEquals(ErrorType.BAD_REQUEST, e.getErrorType());
        }

        // And: Verify API interactions
        verify(productApi, times(1)).findProductsByBarcodes(any());
        verify(inventoryApi, times(1)).validateInventoryAvailability(testProduct1.getId(), 100);
        verify(orderApi, never()).createOrder();
    }

    /**
     * Test creating order with empty order items.
     * Verifies handling of edge case scenarios.
     */
    @Test
    public void testCreateOrder_EmptyOrderItems() {
        // Given: Empty order items list
        List<OrderItemForm> emptyOrderItems = Collections.emptyList();

        // And: Mock API responses
        Map<String, Integer> emptyMap = new HashMap<>();
        when(productApi.findProductsByBarcodes(any())).thenReturn(emptyMap);
        when(orderApi.createOrder()).thenReturn(testOrder);

        // When: OrderFlow creates the order
        OrdersPojo result = orderFlow.createOrder(emptyOrderItems);

        // Then: Result should be valid (empty order is allowed)
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());

        // And: Verify API interactions
        verify(productApi, times(1)).findProductsByBarcodes(any());
        verify(inventoryApi, never()).validateInventoryAvailability(any(), any());
        verify(orderApi, times(1)).createOrder();
        verify(orderItemApi, times(1)).createOrderItemsGroup(any());
        verify(inventoryApi, never()).reduceInventory(any(), any());
    }

    /**
     * Test creating order when order API fails.
     * Verifies error handling for API failures.
     */
    @Test
    public void testCreateOrder_OrderApiFailure() {
        // Given: Valid order item
        OrderItemForm item = new OrderItemForm();
        item.setBarcode(testProduct1.getBarcode());
        item.setQuantity(5);
        item.setMrp(90.0);

        List<OrderItemForm> orderItems = Arrays.asList(item);

        // And: Mock API responses with order creation failure
        Map<String, Integer> barcodeToIdMap = new HashMap<>();
        barcodeToIdMap.put(testProduct1.getBarcode(), testProduct1.getId());
        when(productApi.findProductsByBarcodes(any())).thenReturn(barcodeToIdMap);
        when(orderApi.createOrder()).thenThrow(new ApiException(ErrorType.INTERNAL_SERVER_ERROR, "Order creation failed"));

        // When & Then: OrderFlow should propagate exception
        try {
            orderFlow.createOrder(orderItems);
            fail("Should throw ApiException when order creation fails");
        } catch (ApiException e) {
            assertEquals(ErrorType.INTERNAL_SERVER_ERROR, e.getErrorType());
        }

        // And: Verify API interactions
        verify(productApi, times(1)).findProductsByBarcodes(any());
        verify(inventoryApi, times(1)).validateInventoryAvailability(testProduct1.getId(), 5);
        verify(orderApi, times(1)).createOrder();
        verify(orderItemApi, never()).createOrderItemsGroup(any());
    }

    /**
     * Test creating order with duplicate products.
     * Verifies handling of duplicate barcodes in order items.
     */
    @Test
    public void testCreateOrder_DuplicateProducts() {
        // Given: Order items with duplicate barcodes
        OrderItemForm item1 = new OrderItemForm();
        item1.setBarcode(testProduct1.getBarcode());
        item1.setQuantity(5);
        item1.setMrp(90.0);

        OrderItemForm item2 = new OrderItemForm();
        item2.setBarcode(testProduct1.getBarcode()); // Same barcode
        item2.setQuantity(3);
        item2.setMrp(95.0);

        List<OrderItemForm> orderItems = Arrays.asList(item1, item2);

        // And: Mock API responses
        Map<String, Integer> barcodeToIdMap = new HashMap<>();
        barcodeToIdMap.put(testProduct1.getBarcode(), testProduct1.getId());
        when(productApi.findProductsByBarcodes(any())).thenReturn(barcodeToIdMap);
        when(orderApi.createOrder()).thenReturn(testOrder);

        // When: OrderFlow creates the order
        OrdersPojo result = orderFlow.createOrder(orderItems);

        // Then: Result should be valid (duplicates are processed separately)
        assertNotNull(result);
        assertEquals(testOrder.getId(), result.getId());

        // And: Verify API interactions (both items processed)
        verify(productApi, times(1)).findProductsByBarcodes(any());
        verify(inventoryApi, times(1)).validateInventoryAvailability(testProduct1.getId(), 5);
        verify(inventoryApi, times(1)).validateInventoryAvailability(testProduct1.getId(), 3);
        verify(orderApi, times(1)).createOrder();
        verify(orderItemApi, times(1)).createOrderItemsGroup(any());
    }

    /**
     * Test creating order when inventory reduction fails.
     * Verifies transactional behavior and error handling.
     */
    @Test
    public void testCreateOrder_InventoryReductionFailure() {
        // Given: Valid order item
        OrderItemForm item = new OrderItemForm();
        item.setBarcode(testProduct1.getBarcode());
        item.setQuantity(5);
        item.setMrp(90.0);

        List<OrderItemForm> orderItems = Arrays.asList(item);

        // And: Mock API responses with inventory reduction failure
        Map<String, Integer> barcodeToIdMap = new HashMap<>();
        barcodeToIdMap.put(testProduct1.getBarcode(), testProduct1.getId());
        when(productApi.findProductsByBarcodes(any())).thenReturn(barcodeToIdMap);
        when(orderApi.createOrder()).thenReturn(testOrder);
        doThrow(new ApiException(ErrorType.BAD_REQUEST, "Inventory reduction failed"))
            .when(inventoryApi).reduceInventory(testProduct1.getId(), 5);

        // When & Then: OrderFlow should propagate exception
        try {
            orderFlow.createOrder(orderItems);
            fail("Should throw ApiException when inventory reduction fails");
        } catch (ApiException e) {
            assertEquals(ErrorType.BAD_REQUEST, e.getErrorType());
        }

        // And: Verify API interactions
        verify(productApi, times(1)).findProductsByBarcodes(any());
        verify(inventoryApi, times(1)).validateInventoryAvailability(testProduct1.getId(), 5);
        verify(orderApi, times(1)).createOrder();
        verify(inventoryApi, times(1)).reduceInventory(testProduct1.getId(), 5);
    }
} 