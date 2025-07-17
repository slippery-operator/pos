package com.increff.pos.unit.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.enums.ErrorType;
import com.increff.pos.setup.TestData;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductFlow class.
 * 
 * These tests verify:
 * - Business logic orchestration between API layers
 * - Validation workflows
 * - FK-Id relationship validation
 * - Error handling and exception propagation
 * - Complex business rules implementation
 * 
 * All dependencies are mocked to ensure isolation and fast execution.
 * Focus is on testing the flow logic, not the underlying API implementations.
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductFlowTest {

    @Mock
    private ProductApi productApi;

    @Mock
    private ClientApi clientApi;

    @Mock
    private InventoryApi inventoryApi;

    @InjectMocks
    private ProductFlow productFlow;

    private ProductPojo testProduct;
    private ClientPojo testClient;
    private Integer testClientId;

    @Before
    public void setUp() {
        // Setup test data using TestData factory with proper FK-Id relationships
        testClientId = 1;
        testClient = TestData.client(testClientId, "Test Client");
        testProduct = TestData.product("FLOW123", testClientId, "Flow Test Product", 100.0);
    }

    /**
     * Test validating and creating product - successful case.
     * Verifies that all validation steps are performed and FK-Id relationships are validated.
     */
    @Test
    public void testValidateAndCreateProduct_Success() {
        // Given: Valid product and client exists
        when(clientApi.getClientById(testClientId)).thenReturn(testClient);
        when(productApi.createProduct(testProduct)).thenReturn(testProduct);

        // When: Flow validates and creates product
        ProductPojo result = productFlow.validateAndCreateProduct(testProduct);

        // Then: Product should be created successfully
        assertEquals(testProduct, result);
        assertEquals(testClientId, result.getClientId()); // Verify FK-Id relationship

        // And: All validation steps should be performed
        verify(clientApi).getClientById(testClientId); // FK-Id validation
        verify(productApi).validateBarcodeUniqueness(testProduct.getBarcode(), null);
        verify(productApi).createProduct(testProduct);
        verify(inventoryApi).createInventory(testProduct.getId(), 0); // Inventory initialization
    }

    /**
     * Test validating and creating product - client not found.
     * Verifies that FK-Id validation catches non-existent clients.
     */
    @Test
    public void testValidateAndCreateProduct_ClientNotFound() {
        // Given: Client doesn't exist
        when(clientApi.getClientById(testClientId)).thenThrow(new ApiException(ErrorType.NOT_FOUND, "Client not found"));

        // When & Then: Exception should be thrown
        try {
            productFlow.validateAndCreateProduct(testProduct);
            fail("Expected ApiException to be thrown for non-existent client");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
            assertEquals("Client not found", e.getMessage());
        }

        // And: Product creation should not be attempted
        verify(clientApi).getClientById(testClientId);
        verify(productApi, never()).validateBarcodeUniqueness(anyString(), any());
        verify(productApi, never()).createProduct(any(ProductPojo.class));
    }



    /**
     * Test complex validation workflow.
     * Verifies that multiple validation steps work together correctly.
     */
    @Test
    public void testComplexValidationWorkflow() {
        // Given: Multiple products with different validation scenarios
        ProductPojo validProduct = TestData.product("VALID123", testClientId, "Valid Product", 100.0);
        ProductPojo invalidProduct = TestData.product("INVALID123", 999, "Invalid Product", 200.0);

        when(clientApi.getClientById(testClientId)).thenReturn(testClient);
        when(clientApi.getClientById(999)).thenThrow(new ApiException(ErrorType.NOT_FOUND, "Client not found"));
        when(productApi.createProduct(validProduct)).thenReturn(validProduct);

        // When: Valid product is processed
        ProductPojo result = productFlow.validateAndCreateProduct(validProduct);

        // Then: Valid product should be created
        assertEquals(validProduct, result);
        assertEquals(testClientId, result.getClientId());

        // When: Invalid product is processed
        try {
            productFlow.validateAndCreateProduct(invalidProduct);
            fail("Expected ApiException to be thrown for invalid product");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
        }

        // Then: Verify all interactions
        verify(clientApi).getClientById(testClientId);
        verify(clientApi).getClientById(999);
        verify(productApi).validateBarcodeUniqueness("VALID123", null);
        verify(productApi).createProduct(validProduct);
        verify(productApi, never()).createProduct(invalidProduct);
    }

} 