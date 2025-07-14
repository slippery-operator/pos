package com.increff.pos.unit.api;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.dao.InventoryDao;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.model.response.ValidationError;
import com.increff.pos.setup.TestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.*;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryApi class.
 * 
 * These tests focus on:
 * - Business logic validation
 * - Error handling
 * - Interaction with DAO layer
 * - Proper use of FK-Id relationships (productId)
 * - Inventory availability validation
 * 
 * All dependencies are mocked to ensure isolation and fast execution.
 */
@RunWith(MockitoJUnitRunner.class)
public class InventoryApiTest {

    @Mock
    private InventoryDao inventoryDao;

    @InjectMocks
    private InventoryApi inventoryApi;

    private InventoryPojo testInventory;
    private Integer testProductId;

    @Before
    public void setUp() {
        // Setup test data using TestData factory with proper FK-Id relationships
        testProductId = 1;
        testInventory = TestData.inventory(1, testProductId, 100);
    }

    /**
     * Test searching inventory by product name.
     * Verifies that search parameters are passed correctly to DAO.
     */
    @Test
    public void testSearchInventory_Success() {
        // Given: Search parameters
        String productName = "test";
        int page = 0;
        int size = 10;
        List<InventoryPojo> expectedInventory = Arrays.asList(
            TestData.inventory(1, testProductId, 50),
            TestData.inventory(2, testProductId + 1, 75)
        );

        // When: DAO returns matching inventory
        when(inventoryDao.findByProductNameLike(productName, page, size)).thenReturn(expectedInventory);

        // Then: API should return the same inventory
        List<InventoryPojo> result = inventoryApi.searchInventory(productName, page, size);

        assertEquals(2, result.size());
        assertEquals(expectedInventory, result);
        verify(inventoryDao).findByProductNameLike(productName, page, size);
    }

    /**
     * Test creating inventory successfully.
     * Verifies that inventory is created with proper FK-Id relationships.
     */
    @Test
    public void testCreateInventory_Success() {
        // Given: Product doesn't have inventory yet
        Integer productId = 1;
        Integer quantity = 100;
        when(inventoryDao.selectByProductId(productId)).thenReturn(null);

        // When: API creates inventory
        InventoryPojo result = inventoryApi.createInventory(productId, quantity);

        // Then: Inventory should be created with correct FK-Id
        assertNotNull(result);
        assertEquals(productId, result.getProductId()); // Verify FK-Id relationship
        assertEquals(quantity, result.getQuantity());
        
        verify(inventoryDao).selectByProductId(productId);
        verify(inventoryDao).insert(any(InventoryPojo.class));
    }

    /**
     * Test creating inventory with null quantity.
     * Verifies that null quantity defaults to 0.
     */
    @Test
    public void testCreateInventory_NullQuantity() {
        // Given: Product doesn't have inventory yet and quantity is null
        Integer productId = 1;
        Integer quantity = null;
        when(inventoryDao.selectByProductId(productId)).thenReturn(null);

        // When: API creates inventory
        InventoryPojo result = inventoryApi.createInventory(productId, quantity);

        // Then: Inventory should be created with quantity 0
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(Integer.valueOf(0), result.getQuantity());
        
        verify(inventoryDao).selectByProductId(productId);
        verify(inventoryDao).insert(any(InventoryPojo.class));
    }

    /**
     * Test creating inventory for product that already has inventory.
     * Verifies that appropriate exception is thrown for duplicate inventory.
     */
    @Test
    public void testCreateInventory_AlreadyExists() {
        // Given: Product already has inventory
        Integer productId = 1;
        Integer quantity = 100;
        InventoryPojo existingInventory = TestData.inventory(1, productId, 50);
        when(inventoryDao.selectByProductId(productId)).thenReturn(existingInventory);

        // When & Then: Exception should be thrown
        try {
            inventoryApi.createInventory(productId, quantity);
            fail("Expected ApiException to be thrown for existing inventory");
        } catch (ApiException e) {
            assertEquals(ErrorType.CONFLICT, e.getErrorType());
            assertTrue(e.getMessage().contains("already exists"));
        }

        // And: Insert should not be called
        verify(inventoryDao).selectByProductId(productId);
        verify(inventoryDao, never()).insert(any(InventoryPojo.class));
    }

    /**
     * Test updating inventory by product ID successfully.
     * Verifies that inventory is updated with proper FK-Id relationships.
     */
    @Test
    public void testUpdateInventoryByProductId_Success() {
        // Given: Inventory exists for product
        Integer productId = 1;
        Integer newQuantity = 200;
        InventoryPojo existingInventory = TestData.inventory(1, productId, 100);
        when(inventoryDao.selectByProductId(productId)).thenReturn(existingInventory);

        // When: API updates inventory
        InventoryPojo result = inventoryApi.updateInventoryByProductId(productId, newQuantity);

        // Then: Inventory should be updated
        assertNotNull(result);
        assertEquals(productId, result.getProductId()); // Verify FK-Id relationship
        assertEquals(newQuantity, result.getQuantity());
        
        verify(inventoryDao).selectByProductId(productId);
    }

    /**
     * Test updating inventory for non-existent product.
     * Verifies that appropriate exception is thrown when inventory doesn't exist.
     */
    @Test
    public void testUpdateInventoryByProductId_NotFound() {
        // Given: Inventory doesn't exist for product
        Integer productId = 999;
        Integer newQuantity = 200;
        when(inventoryDao.selectByProductId(productId)).thenReturn(null);

        // When & Then: Exception should be thrown
        try {
            inventoryApi.updateInventoryByProductId(productId, newQuantity);
            fail("Expected ApiException to be thrown for non-existent inventory");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
            assertEquals("Inventory not found for product", e.getMessage());
        }

        verify(inventoryDao).selectByProductId(productId);
    }

    /**
     * Test validating inventory availability - sufficient stock.
     * Verifies that validation passes when sufficient stock is available.
     */
    @Test
    public void testValidateInventoryAvailability_SufficientStock() {
        // Given: Inventory has sufficient stock
        Integer productId = 1;
        Integer requiredQuantity = 50;
        InventoryPojo inventory = TestData.inventory(1, productId, 100);
        when(inventoryDao.selectByProductId(productId)).thenReturn(inventory);

        // When & Then: No exception should be thrown
        inventoryApi.validateInventoryAvailability(productId, requiredQuantity);

        verify(inventoryDao).selectByProductId(productId);
    }

    /**
     * Test validating inventory availability - insufficient stock.
     * Verifies that validation fails when insufficient stock is available.
     */
    @Test
    public void testValidateInventoryAvailability_InsufficientStock() {
        // Given: Inventory has insufficient stock
        Integer productId = 1;
        Integer requiredQuantity = 150;
        InventoryPojo inventory = TestData.inventory(1, productId, 100);
        when(inventoryDao.selectByProductId(productId)).thenReturn(inventory);

        // When & Then: Exception should be thrown
        try {
            inventoryApi.validateInventoryAvailability(productId, requiredQuantity);
            fail("Expected ApiException to be thrown for insufficient stock");
        } catch (ApiException e) {
            assertEquals(ErrorType.BAD_REQUEST, e.getErrorType());
            assertTrue(e.getMessage().contains("Insufficient inventory"));
        }

        verify(inventoryDao).selectByProductId(productId);
    }

    /**
     * Test validating inventory availability - inventory not found.
     * Verifies that validation fails when inventory doesn't exist.
     */
    @Test
    public void testValidateInventoryAvailability_InventoryNotFound() {
        // Given: Inventory doesn't exist for product
        Integer productId = 999;
        Integer requiredQuantity = 50;
        when(inventoryDao.selectByProductId(productId)).thenReturn(null);

        // When & Then: Exception should be thrown
        try {
            inventoryApi.validateInventoryAvailability(productId, requiredQuantity);
            fail("Expected ApiException to be thrown for non-existent inventory");
        } catch (ApiException e) {
            assertEquals(ErrorType.BAD_REQUEST, e.getErrorType());
            assertTrue(e.getMessage().contains("No inventory found"));
        }

        verify(inventoryDao).selectByProductId(productId);
    }

    /**
     * Test reducing inventory successfully.
     * Verifies that inventory quantity is reduced correctly.
     */
    @Test
    public void testReduceInventory_Success() {
        // Given: Inventory has sufficient stock
        Integer productId = 1;
        Integer orderQuantity = 30;
        InventoryPojo inventory = TestData.inventory(1, productId, 100);
        when(inventoryDao.selectByProductId(productId)).thenReturn(inventory);

        // When: API reduces inventory
        inventoryApi.reduceInventory(productId, orderQuantity);

        // Then: Inventory quantity should be reduced
        assertEquals(Integer.valueOf(70), inventory.getQuantity());
        verify(inventoryDao, times(2)).selectByProductId(productId); // Called twice: once in validateInventoryAvailability, once in reduceInventory
    }

    /**
     * Test reducing inventory with insufficient stock.
     * Verifies that appropriate exception is thrown when trying to reduce more than available.
     */
    @Test
    public void testReduceInventory_InsufficientStock() {
        // Given: Inventory has insufficient stock
        Integer productId = 1;
        Integer orderQuantity = 150;
        InventoryPojo inventory = TestData.inventory(1, productId, 100);
        when(inventoryDao.selectByProductId(productId)).thenReturn(inventory);

        // When & Then: Exception should be thrown
        try {
            inventoryApi.reduceInventory(productId, orderQuantity);
            fail("Expected ApiException to be thrown for insufficient stock");
        } catch (ApiException e) {
            assertEquals(ErrorType.BAD_REQUEST, e.getErrorType());
            assertTrue(e.getMessage().contains("Insufficient inventory"));
        }

        // And: Inventory quantity should remain unchanged
        assertEquals(Integer.valueOf(100), inventory.getQuantity());
        verify(inventoryDao).selectByProductId(productId);
    }

    /**
     * Test bulk creating inventory successfully.
     * Verifies that multiple inventory records are created with proper FK-Id relationships.
     */
    @Test
    public void testBulkCreateInventory_Success() {
        // Given: List of product IDs
        List<Integer> productIds = Arrays.asList(1, 2, 3);
        
        // And: Mock the return from selectByProductIds
        List<InventoryPojo> expectedInventories = Arrays.asList(
            TestData.inventory(1, 0),
            TestData.inventory(2, 0),
            TestData.inventory(3, 0)
        );
        when(inventoryDao.selectByProductIds(productIds)).thenReturn(expectedInventories);

        // When: API creates bulk inventory
        List<InventoryPojo> result = inventoryApi.bulkCreateInventory(productIds);

        // Then: All inventory records should be created
        assertEquals(3, result.size());
        for (int i = 0; i < result.size(); i++) {
            InventoryPojo inventory = result.get(i);
            assertEquals(productIds.get(i), inventory.getProductId()); // Verify FK-Id relationships
            assertEquals(Integer.valueOf(0), inventory.getQuantity()); // Default quantity
        }

        verify(inventoryDao).bulkInsert(productIds);
    }

    /**
     * Test bulk creating or updating inventory successfully.
     * Verifies that inventory records are created or updated with proper FK-Id relationships.
     */
    @Test
    public void testBulkCreateOrUpdateInventory_Success() {
        // Given: List of inventory to create/update
        List<InventoryPojo> inventoryList = Arrays.asList(
            TestData.inventory(1, 50),
            TestData.inventory(2, 75),
            TestData.inventory(3, 100)
        );
        
        // And: Mock the return from selectByProductIds
        List<Integer> productIds = Arrays.asList(1, 2, 3);
        when(inventoryDao.selectByProductIds(productIds)).thenReturn(inventoryList);

        // When: API creates or updates bulk inventory
        List<InventoryPojo> result = inventoryApi.bulkCreateOrUpdateInventory(inventoryList);

        // Then: All inventory records should be processed
        assertEquals(3, result.size());
        for (int i = 0; i < result.size(); i++) {
            InventoryPojo inventory = result.get(i);
            assertEquals(inventoryList.get(i).getProductId(), inventory.getProductId()); // Verify FK-Id relationships
            assertEquals(inventoryList.get(i).getQuantity(), inventory.getQuantity());
        }

        verify(inventoryDao).bulkUpsert(inventoryList);
    }

    /**
     * Test validating inventory without saving - all valid.
     * Verifies that validation returns no errors for valid inventory.
     */
    @Test
    public void testValidateInventoryWithoutSaving_AllValid() {
        // Given: Valid inventory data
        Map<Integer, Integer> rowByProductId = new HashMap<>();
        rowByProductId.put(1, 50);
        rowByProductId.put(2, 75);
        rowByProductId.put(3, 100);

        // When: API validates inventory
        Map<Integer, ValidationError> result = inventoryApi.validateInventoryWithoutSaving(rowByProductId);

        // Then: No validation errors should be returned
        assertEquals(0, result.size());
    }

    /**
     * Test validating inventory without saving - some invalid.
     * Verifies that validation returns errors for invalid inventory.
     */
    @Test
    public void testValidateInventoryWithoutSaving_SomeInvalid() {
        // Given: Invalid inventory data (negative quantities)
        Map<Integer, Integer> rowByProductId = new HashMap<>();
        rowByProductId.put(1, 50);    // Valid
        rowByProductId.put(2, -10);   // Invalid - negative quantity
        rowByProductId.put(3, 100);   // Valid

        // When: API validates inventory
        Map<Integer, ValidationError> result = inventoryApi.validateInventoryWithoutSaving(rowByProductId);

        // Then: Validation errors should be returned for invalid entries
        assertEquals(1, result.size());
        assertTrue(result.containsKey(2));
        ValidationError error = result.get(2);
        assertNotNull(error);
        assertTrue(error.getErrorMessage().contains("negative") || error.getErrorMessage().contains("invalid"));
    }

    /**
     * Test validating inventory without saving - empty input.
     * Verifies that empty input is handled correctly.
     */
    @Test
    public void testValidateInventoryWithoutSaving_EmptyInput() {
        // Given: Empty inventory data
        Map<Integer, Integer> emptyRowByProductId = new HashMap<>();

        // When: API validates inventory
        Map<Integer, ValidationError> result = inventoryApi.validateInventoryWithoutSaving(emptyRowByProductId);

        // Then: No validation errors should be returned
        assertEquals(0, result.size());
    }

    /**
     * Test validating inventory without saving - null input.
     * Verifies that null input is handled correctly.
     */
    @Test
    public void testValidateInventoryWithoutSaving_NullInput() {
        // Given: Null inventory data
        Map<Integer, Integer> nullRowByProductId = null;

        // When & Then: API should throw NullPointerException for null input
        try {
            inventoryApi.validateInventoryWithoutSaving(nullRowByProductId);
            fail("Expected NullPointerException to be thrown");
        } catch (NullPointerException e) {
            assertTrue("Should throw NullPointerException for null input", 
                e.getMessage().contains("Input map cannot be null"));
        }
    }

    /**
     * Test edge case - zero quantity validation.
     * Verifies that zero quantity is handled correctly.
     */
    @Test
    public void testCreateInventory_ZeroQuantity() {
        // Given: Product doesn't have inventory yet and quantity is zero
        Integer productId = 1;
        Integer quantity = 0;
        when(inventoryDao.selectByProductId(productId)).thenReturn(null);

        // When: API creates inventory
        InventoryPojo result = inventoryApi.createInventory(productId, quantity);

        // Then: Inventory should be created with quantity 0
        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(Integer.valueOf(0), result.getQuantity());
        
        verify(inventoryDao).selectByProductId(productId);
        verify(inventoryDao).insert(any(InventoryPojo.class));
    }

    /**
     * Test edge case - exact quantity match for availability.
     * Verifies that validation passes when required quantity exactly matches available quantity.
     */
    @Test
    public void testValidateInventoryAvailability_ExactMatch() {
        // Given: Inventory has exactly the required quantity
        Integer productId = 1;
        Integer requiredQuantity = 100;
        InventoryPojo inventory = TestData.inventory(1, productId, 100);
        when(inventoryDao.selectByProductId(productId)).thenReturn(inventory);

        // When & Then: No exception should be thrown
        inventoryApi.validateInventoryAvailability(productId, requiredQuantity);

        verify(inventoryDao).selectByProductId(productId);
    }
} 