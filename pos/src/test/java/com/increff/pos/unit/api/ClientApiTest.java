package com.increff.pos.unit.api;

import com.increff.pos.api.ClientApi;
import com.increff.pos.dao.ClientDao;
import com.increff.pos.entity.ClientPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
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
 * Unit tests for ClientApi class.
 * 
 * These tests focus on:
 * - Business logic validation
 * - Error handling
 * - Interaction with DAO layer
 * - Client name uniqueness validation
 * - Batch validation operations
 * 
 * All dependencies are mocked to ensure isolation and fast execution.
 */
@RunWith(MockitoJUnitRunner.class)
public class ClientApiTest {

    @Mock
    private ClientDao clientDao;

    @InjectMocks
    private ClientApi clientApi;

    private ClientPojo testClient;

    @Before
    public void setUp() {
        // Setup test data using TestData factory
        testClient = TestData.client(1, "Test Client");
    }

    /**
     * Test adding a client successfully.
     * Verifies that client is created when name is unique.
     */
    @Test
    public void testAdd_Success() {
        // Given: Client name doesn't exist
        String clientName = "New Client";
        when(clientDao.selectByName(clientName)).thenReturn(null);

        // When: Client is added
        ClientPojo result = clientApi.add(clientName);

        // Then: Client should be created and inserted
        assertNotNull(result);
        assertEquals(clientName, result.getName());
        verify(clientDao).selectByName(clientName);
        verify(clientDao).insert(any(ClientPojo.class));
    }

    /**
     * Test adding a client with duplicate name.
     * Verifies that appropriate exception is thrown for duplicate names.
     */
    @Test
    public void testAdd_DuplicateName() {
        // Given: Client name already exists
        String duplicateName = "Existing Client";
        ClientPojo existingClient = TestData.client(1, duplicateName);
        when(clientDao.selectByName(duplicateName)).thenReturn(existingClient);

        // When & Then: Exception should be thrown
        try {
            clientApi.add(duplicateName);
            fail("Expected ApiException to be thrown for duplicate name");
        } catch (ApiException e) {
            assertEquals(ErrorType.CONFLICT, e.getErrorType());
            assertTrue(e.getMessage().contains("already exists"));
        }

        // And: Insert should not be called
        verify(clientDao).selectByName(duplicateName);
        verify(clientDao, never()).insert(any(ClientPojo.class));
    }

    /**
     * Test adding a client with null name.
     * Verifies that null safety is handled properly.
     */
    @Test
    public void testAdd_NullName() {
        // Given: Null client name
        String nullName = null;

        // When & Then: Exception should be thrown
        try {
            clientApi.add(nullName);
            fail("Expected exception to be thrown for null name");
        } catch (Exception e) {
            // Some form of exception should be thrown
            assertNotNull(e);
        }
    }

    /**
     * Test updating a client successfully.
     * Verifies that client is updated when new name is unique.
     */
    @Test
    public void testUpdate_Success() {
        // Given: Client exists and new name is unique
        Integer clientId = 1;
        String newName = "Updated Client";
        ClientPojo existingClient = TestData.client(clientId, "Original Client");
        
        when(clientDao.selectByName(newName)).thenReturn(null);
        when(clientDao.selectById(clientId)).thenReturn(existingClient);

        // When: Client is updated
        ClientPojo result = clientApi.update(clientId, newName);

        // Then: Client should be updated
        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(clientId, result.getClientId());
        
        verify(clientDao).selectByName(newName);
        verify(clientDao).selectById(clientId);
    }

    /**
     * Test updating a client with duplicate name.
     * Verifies that exception is thrown when new name already exists for different client.
     */
    @Test
    public void testUpdate_DuplicateName() {
        // Given: New name already exists for different client
        Integer clientId = 1;
        String duplicateName = "Existing Client";
        ClientPojo existingClient = TestData.client(2, duplicateName); // Different ID
        
        when(clientDao.selectByName(duplicateName)).thenReturn(existingClient);

        // When & Then: Exception should be thrown
        try {
            clientApi.update(clientId, duplicateName);
            fail("Expected ApiException to be thrown for duplicate name");
        } catch (ApiException e) {
            assertEquals(ErrorType.CONFLICT, e.getErrorType());
            assertTrue(e.getMessage().contains("already exists"));
        }

        // And: Client should not be retrieved for update
        verify(clientDao).selectByName(duplicateName);
        verify(clientDao, never()).selectById(clientId);
    }

    /**
     * Test updating a client with same name (no change).
     * Verifies that client can keep the same name.
     */
    @Test
    public void testUpdate_SameName() {
        // Given: Client exists and name is same as current
        Integer clientId = 1;
        String sameName = "Same Client";
        ClientPojo existingClient = TestData.client(clientId, sameName);
        
        when(clientDao.selectByName(sameName)).thenReturn(existingClient);
        when(clientDao.selectById(clientId)).thenReturn(existingClient);

        // When: Client is updated with same name
        ClientPojo result = clientApi.update(clientId, sameName);

        // Then: Update should succeed
        assertNotNull(result);
        assertEquals(sameName, result.getName());
        assertEquals(clientId, result.getClientId());
        
        verify(clientDao).selectByName(sameName);
        verify(clientDao).selectById(clientId);
    }

    /**
     * Test updating a non-existent client.
     * Verifies that appropriate exception is thrown when client doesn't exist.
     */
    @Test
    public void testUpdate_ClientNotFound() {
        // Given: Client doesn't exist
        Integer nonExistentId = 999;
        String newName = "New Name";
        
        when(clientDao.selectByName(newName)).thenReturn(null);
        when(clientDao.selectById(nonExistentId)).thenReturn(null);

        // When & Then: Exception should be thrown
        try {
            clientApi.update(nonExistentId, newName);
            fail("Expected ApiException to be thrown for non-existent client");
        } catch (ApiException e) {
            assertEquals(ErrorType.NOT_FOUND, e.getErrorType());
            assertEquals("Client not found", e.getMessage());
        }

        verify(clientDao).selectByName(newName);
        verify(clientDao).selectById(nonExistentId);
    }

    /**
     * Test getting all clients with pagination.
     * Verifies that pagination parameters are passed correctly to DAO.
     */
    @Test
    public void testGetAll_Success() {
        // Given: Multiple clients exist
        int page = 0;
        int size = 10;
        List<ClientPojo> expectedClients = Arrays.asList(
            TestData.client(1, "Client A"),
            TestData.client(2, "Client B"),
            TestData.client(3, "Client C")
        );
        
        when(clientDao.selectAll(page, size)).thenReturn(expectedClients);

        // When: All clients are retrieved
        List<ClientPojo> result = clientApi.getAll(page, size);

        // Then: Clients should be returned
        assertEquals(3, result.size());
        assertEquals(expectedClients, result);
        verify(clientDao).selectAll(page, size);
    }

    /**
     * Test getting all clients - empty result.
     * Verifies that empty list is handled correctly.
     */
    @Test
    public void testGetAll_EmptyResult() {
        // Given: No clients exist
        int page = 0;
        int size = 10;
        when(clientDao.selectAll(page, size)).thenReturn(Collections.emptyList());

        // When: All clients are retrieved
        List<ClientPojo> result = clientApi.getAll(page, size);

        // Then: Empty list should be returned
        assertEquals(0, result.size());
        verify(clientDao).selectAll(page, size);
    }

    /**
     * Test searching clients by name.
     * Verifies that search functionality works correctly.
     */
    @Test
    public void testSearchByName_Success() {
        // Given: Clients with matching names exist
        String searchName = "test";
        int page = 0;
        int size = 10;
        List<ClientPojo> expectedClients = Arrays.asList(
            TestData.client(1, "Test Client 1"),
            TestData.client(2, "Test Client 2")
        );
        
        when(clientDao.selectByNameContaining(searchName, page, size)).thenReturn(expectedClients);

        // When: Clients are searched by name
        List<ClientPojo> result = clientApi.searchByName(searchName, page, size);

        // Then: Matching clients should be returned
        assertEquals(2, result.size());
        assertEquals(expectedClients, result);
        verify(clientDao).selectByNameContaining(searchName, page, size);
    }

    /**
     * Test searching clients by name - no matches.
     * Verifies that empty result is handled correctly.
     */
    @Test
    public void testSearchByName_NoMatches() {
        // Given: No clients match the search
        String searchName = "nonexistent";
        int page = 0;
        int size = 10;
        when(clientDao.selectByNameContaining(searchName, page, size)).thenReturn(Collections.emptyList());

        // When: Clients are searched by name
        List<ClientPojo> result = clientApi.searchByName(searchName, page, size);

        // Then: Empty list should be returned
        assertEquals(0, result.size());
        verify(clientDao).selectByNameContaining(searchName, page, size);
    }

    /**
     * Test batch validation of client existence - all exist.
     * Verifies that batch validation works correctly when all clients exist.
     */
    @Test
    public void testValidateClientsExistBatch_AllExist() {
        // Given: All client IDs exist
        Set<Integer> clientIds = new HashSet<>(Arrays.asList(1, 2, 3));
        List<ClientPojo> existingClients = Arrays.asList(
            TestData.client(1, "Client 1"),
            TestData.client(2, "Client 2"),
            TestData.client(3, "Client 3")
        );
        
        when(clientDao.selectByIds(clientIds)).thenReturn(existingClients);

        // When: Batch validation is performed
        Map<Integer, Boolean> result = clientApi.validateClientsExistBatch(clientIds);

        // Then: All should be marked as existing
        assertEquals(3, result.size());
        assertTrue(result.get(1));
        assertTrue(result.get(2));
        assertTrue(result.get(3));
        verify(clientDao).selectByIds(clientIds);
    }

    /**
     * Test batch validation of client existence - some exist.
     * Verifies that batch validation correctly identifies existing vs non-existing clients.
     */
    @Test
    public void testValidateClientsExistBatch_SomeExist() {
        // Given: Some client IDs exist, some don't
        Set<Integer> clientIds = new HashSet<>(Arrays.asList(1, 2, 3, 4));
        List<ClientPojo> existingClients = Arrays.asList(
            TestData.client(1, "Client 1"),
            TestData.client(3, "Client 3")
        );
        
        when(clientDao.selectByIds(clientIds)).thenReturn(existingClients);

        // When: Batch validation is performed
        Map<Integer, Boolean> result = clientApi.validateClientsExistBatch(clientIds);

        // Then: Existing clients should be true, non-existing should be false
        assertEquals(4, result.size());
        assertTrue(result.get(1));   // Exists
        assertFalse(result.get(2));  // Doesn't exist
        assertTrue(result.get(3));   // Exists
        assertFalse(result.get(4));  // Doesn't exist
        verify(clientDao).selectByIds(clientIds);
    }

    /**
     * Test batch validation of client existence - empty input.
     * Verifies that empty input is handled correctly.
     */
    @Test
    public void testValidateClientsExistBatch_EmptyInput() {
        // Given: Empty client IDs set
        Set<Integer> emptyClientIds = Collections.emptySet();

        // When: Batch validation is performed
        Map<Integer, Boolean> result = clientApi.validateClientsExistBatch(emptyClientIds);

        // Then: Empty map should be returned
        assertEquals(0, result.size());
        verify(clientDao, never()).selectByIds(any());
    }

    /**
     * Test batch validation of client existence - null input.
     * Verifies that null input is handled correctly.
     */
    @Test
    public void testValidateClientsExistBatch_NullInput() {
        // Given: Null client IDs set
        Set<Integer> nullClientIds = null;

        // When: Batch validation is performed
        Map<Integer, Boolean> result = clientApi.validateClientsExistBatch(nullClientIds);

        // Then: Empty map should be returned
        assertEquals(0, result.size());
        verify(clientDao, never()).selectByIds(any());
    }
} 