package com.increff.pos.unit.api;

import com.increff.pos.api.ReportApi;
import com.increff.pos.dao.ReportDao;
import com.increff.pos.entity.DaySalesPojo;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ReportApi class.
 * 
 * These tests verify:
 * - Sales report generation functionality
 * - Date-based filtering and queries
 * - Day sales management operations
 * - Database interaction through DAO layer
 * - Business logic validation
 * 
 * Uses mocked dependencies to isolate the API layer
 * and test business logic without database dependencies.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReportApiTest {

    @Mock
    private ReportDao reportDao;

    @InjectMocks
    private ReportApi reportApi;

    private DaySalesPojo testDaySales1;
    private DaySalesPojo testDaySales2;
    private ZonedDateTime testDate1;
    private ZonedDateTime testDate2;
    private ZonedDateTime startDate;
    private ZonedDateTime endDate;

    @Before
    public void setUp() {
        // Set up test data using TestData utility
        testDate1 = ZonedDateTime.of(2024, 1, 15, 0, 0, 0, 0, ZoneOffset.UTC);
        testDate2 = ZonedDateTime.of(2024, 1, 16, 0, 0, 0, 0, ZoneOffset.UTC);
        startDate = ZonedDateTime.of(2024, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC);
        endDate = ZonedDateTime.of(2024, 1, 20, 0, 0, 0, 0, ZoneOffset.UTC);

        testDaySales1 = TestData.daySales(1, testDate1, 10, 25, 2500.0);
        testDaySales2 = TestData.daySales(2, testDate2, 15, 30, 3000.0);
    }

    /**
     * Test getting day sales by date range successfully.
     * Verifies that the method correctly queries the DAO with date range.
     */
    @Test
    public void testGetDaySalesByDateRange_Success() {
        // Given: Mock DAO returns list of day sales
        List<DaySalesPojo> expectedSales = Arrays.asList(testDaySales1, testDaySales2);
        when(reportDao.getDaySalesByDateRange(startDate, endDate)).thenReturn(expectedSales);

        // When: Get day sales by date range
        List<DaySalesPojo> result = reportApi.getDaySalesByDateRange(startDate, endDate);

        // Then: Verify correct data is returned and DAO is called
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 2 day sales records", 2, result.size());
        assertEquals("First record should match", testDaySales1.getId(), result.get(0).getId());
        assertEquals("Second record should match", testDaySales2.getId(), result.get(1).getId());
        verify(reportDao, times(1)).getDaySalesByDateRange(startDate, endDate);
    }

    /**
     * Test getting day sales by date range with empty result.
     * Verifies that empty list is handled correctly.
     */
    @Test
    public void testGetDaySalesByDateRange_EmptyResult() {
        // Given: Mock DAO returns empty list
        when(reportDao.getDaySalesByDateRange(startDate, endDate)).thenReturn(Collections.emptyList());

        // When: Get day sales by date range
        List<DaySalesPojo> result = reportApi.getDaySalesByDateRange(startDate, endDate);

        // Then: Verify empty list is returned
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
        verify(reportDao, times(1)).getDaySalesByDateRange(startDate, endDate);
    }

    /**
     * Test getting day sales by date range with null dates.
     * Verifies that null parameters are passed through to DAO.
     */
    @Test
    public void testGetDaySalesByDateRange_NullDates() {
        // Given: Mock DAO handles null dates
        when(reportDao.getDaySalesByDateRange(null, null)).thenReturn(Collections.emptyList());

        // When: Get day sales with null dates
        List<DaySalesPojo> result = reportApi.getDaySalesByDateRange(null, null);

        // Then: Verify DAO is called with null parameters
        assertNotNull("Result should not be null", result);
        verify(reportDao, times(1)).getDaySalesByDateRange(null, null);
    }

    /**
     * Test saving day sales successfully.
     * Verifies that the method correctly delegates to DAO insert.
     */
    @Test
    public void testSaveDaySales_Success() {
        // Given: Mock DAO insert operation
        doNothing().when(reportDao).insert(testDaySales1);

        // When: Save day sales
        reportApi.saveDaySales(testDaySales1);

        // Then: Verify DAO insert is called
        verify(reportDao, times(1)).insert(testDaySales1);
    }

    /**
     * Test saving day sales with null input.
     * Verifies that null parameter is passed through to DAO.
     */
    @Test
    public void testSaveDaySales_NullInput() {
        // Given: Mock DAO handles null input
        doNothing().when(reportDao).insert(null);

        // When: Save null day sales
        reportApi.saveDaySales(null);

        // Then: Verify DAO is called with null parameter
        verify(reportDao, times(1)).insert(null);
    }

    /**
     * Test getting day sales by specific date successfully.
     * Verifies that the method correctly queries the DAO with specific date.
     */
    @Test
    public void testGetDaySalesByDate_Success() {
        // Given: Mock DAO returns day sales for specific date
        when(reportDao.getDaySalesByDate(testDate1)).thenReturn(testDaySales1);

        // When: Get day sales by date
        DaySalesPojo result = reportApi.getDaySalesByDate(testDate1);

        // Then: Verify correct data is returned
        assertNotNull("Result should not be null", result);
        assertEquals("Should return correct day sales", testDaySales1.getId(), result.getId());
        assertEquals("Date should match", testDate1, result.getDate());
        assertEquals("Revenue should match", testDaySales1.getTotalRevenue(), result.getTotalRevenue());
        verify(reportDao, times(1)).getDaySalesByDate(testDate1);
    }

    /**
     * Test getting day sales by date with no result.
     * Verifies that null result is handled correctly.
     */
    @Test
    public void testGetDaySalesByDate_NoResult() {
        // Given: Mock DAO returns null for date with no sales
        when(reportDao.getDaySalesByDate(testDate1)).thenReturn(null);

        // When: Get day sales by date
        DaySalesPojo result = reportApi.getDaySalesByDate(testDate1);

        // Then: Verify null is returned
        assertNull("Result should be null when no sales found", result);
        verify(reportDao, times(1)).getDaySalesByDate(testDate1);
    }

    /**
     * Test getting day sales by null date.
     * Verifies that null parameter is passed through to DAO.
     */
    @Test
    public void testGetDaySalesByDate_NullDate() {
        // Given: Mock DAO handles null date
        when(reportDao.getDaySalesByDate(null)).thenReturn(null);

        // When: Get day sales with null date
        DaySalesPojo result = reportApi.getDaySalesByDate(null);

        // Then: Verify DAO is called with null parameter
        assertNull("Result should be null", result);
        verify(reportDao, times(1)).getDaySalesByDate(null);
    }

    /**
     * Test updating day sales successfully.
     * Verifies that the method correctly delegates to DAO update.
     */
    @Test
    public void testUpdateDaySales_Success() {
        // Given: Mock DAO update operation
        doNothing().when(reportDao).update(testDaySales1);

        // When: Update day sales
        reportApi.updateDaySales(testDaySales1);

        // Then: Verify DAO update is called
        verify(reportDao, times(1)).update(testDaySales1);
    }

    /**
     * Test updating day sales with modified data.
     * Verifies that updated data is passed correctly to DAO.
     */
    @Test
    public void testUpdateDaySales_ModifiedData() {
        // Given: Modify test data
        testDaySales1.setTotalRevenue(5000.0);
        testDaySales1.setInvoicedOrdersCount(20);
        testDaySales1.setInvoicedItemsCount(50);
        doNothing().when(reportDao).update(testDaySales1);

        // When: Update day sales with modified data
        reportApi.updateDaySales(testDaySales1);

        // Then: Verify DAO is called with modified data
        verify(reportDao, times(1)).update(testDaySales1);
    }

    /**
     * Test updating day sales with null input.
     * Verifies that null parameter is passed through to DAO.
     */
    @Test
    public void testUpdateDaySales_NullInput() {
        // Given: Mock DAO handles null input
        doNothing().when(reportDao).update(null);

        // When: Update null day sales
        reportApi.updateDaySales(null);

        // Then: Verify DAO is called with null parameter
        verify(reportDao, times(1)).update(null);
    }

    /**
     * Test getting day sales with same start and end date.
     * Verifies that single day range query works correctly.
     */
    @Test
    public void testGetDaySalesByDateRange_SameDate() {
        // Given: Mock DAO returns single day sales for same date range
        List<DaySalesPojo> expectedSales = Arrays.asList(testDaySales1);
        when(reportDao.getDaySalesByDateRange(testDate1, testDate1)).thenReturn(expectedSales);

        // When: Get day sales for same start and end date
        List<DaySalesPojo> result = reportApi.getDaySalesByDateRange(testDate1, testDate1);

        // Then: Verify single record is returned
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 1 day sales record", 1, result.size());
        assertEquals("Record should match", testDaySales1.getId(), result.get(0).getId());
        verify(reportDao, times(1)).getDaySalesByDateRange(testDate1, testDate1);
    }

    /**
     * Test getting day sales with reverse date range.
     * Verifies that end date before start date is handled by DAO.
     */
    @Test
    public void testGetDaySalesByDateRange_ReverseDateRange() {
        // Given: Mock DAO handles reverse date range
        when(reportDao.getDaySalesByDateRange(endDate, startDate)).thenReturn(Collections.emptyList());

        // When: Get day sales with reverse date range
        List<DaySalesPojo> result = reportApi.getDaySalesByDateRange(endDate, startDate);

        // Then: Verify DAO is called with reverse dates
        assertNotNull("Result should not be null", result);
        verify(reportDao, times(1)).getDaySalesByDateRange(endDate, startDate);
    }

    /**
     * Test data consistency across multiple operations.
     * Verifies that multiple API calls work correctly together.
     */
    @Test
    public void testMultipleOperations_DataConsistency() {
        // Given: Mock DAO operations with sequential behavior
        when(reportDao.getDaySalesByDate(testDate1))
            .thenReturn(null)  // First call returns null
            .thenReturn(testDaySales1);  // Second call returns the object
        doNothing().when(reportDao).insert(testDaySales1);
        doNothing().when(reportDao).update(testDaySales1);

        // When: Perform multiple operations
        DaySalesPojo initial = reportApi.getDaySalesByDate(testDate1);
        reportApi.saveDaySales(testDaySales1);
        DaySalesPojo saved = reportApi.getDaySalesByDate(testDate1);
        reportApi.updateDaySales(testDaySales1);

        // Then: Verify all operations were called
        assertNull("Initial result should be null", initial);
        assertNotNull("Saved result should not be null", saved);
        verify(reportDao, times(2)).getDaySalesByDate(testDate1);
        verify(reportDao, times(1)).insert(testDaySales1);
        verify(reportDao, times(1)).update(testDaySales1);
    }
} 