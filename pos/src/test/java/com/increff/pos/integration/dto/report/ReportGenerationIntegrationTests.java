package com.increff.pos.integration.dto.report;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.model.response.DaySalesResponse;
import com.increff.pos.setup.AbstractIntegrationTest;
import com.increff.pos.setup.TestData;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.ZonedDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Integration tests for ReportDto class.
 * 
 * These tests verify:
 * - End-to-end report generation workflow
 * - Database persistence and retrieval
 * - Date parsing and formatting
 * - Response conversion functionality
 * - Business logic integration across layers
 * 
 * Each test focuses on exactly one DTO method and verifies both
 * the return value and the actual database state.
 */
public class ReportGenerationIntegrationTests extends AbstractIntegrationTest {

    @Autowired
    private ReportDto reportDto;

    /**
     * Test getting day sales by date range successfully.
     * Verifies complete workflow from string date parsing to response conversion.
     */
    @Test
    public void testGetDaySalesByDateRange_Success() {
        // Given: Create and persist test day sales data
        ZonedDateTime date1 = ZonedDateTime.of(2024, 1, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime date2 = ZonedDateTime.of(2024, 1, 16, 14, 30, 0, 0, ZoneOffset.UTC);
        ZonedDateTime date3 = ZonedDateTime.of(2024, 1, 17, 16, 45, 0, 0, ZoneOffset.UTC);
        
        DaySalesPojo daySales1 = TestData.daySales(date1, 5, 15, 1500.0);
        DaySalesPojo daySales2 = TestData.daySales(date2, 8, 20, 2000.0);
        DaySalesPojo daySales3 = TestData.daySales(date3, 3, 10, 800.0);
        
        reportDao.insert(daySales1);
        reportDao.insert(daySales2);
        reportDao.insert(daySales3);

        // When: Get day sales by date range using string dates
        List<DaySalesResponse> result = reportDto.getDaySalesByDateRange("2024-01-15", "2024-01-16");

        // Then: Verify results and database state
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 2 day sales records", 2, result.size());
        
        // Verify first record
        DaySalesResponse first = result.get(0);
        assertEquals("First record orders count should match", Integer.valueOf(5), first.getInvoicedOrdersCount());
        assertEquals("First record items count should match", Integer.valueOf(15), first.getInvoicedItemsCount());
        assertEquals("First record revenue should match", Double.valueOf(1500.0), first.getTotalRevenue());
        
        // Verify second record
        DaySalesResponse second = result.get(1);
        assertEquals("Second record orders count should match", Integer.valueOf(8), second.getInvoicedOrdersCount());
        assertEquals("Second record items count should match", Integer.valueOf(20), second.getInvoicedItemsCount());
        assertEquals("Second record revenue should match", Double.valueOf(2000.0), second.getTotalRevenue());
        
        // Verify database state - should still contain all 3 records
        List<DaySalesPojo> allSales = reportDao.getDaySalesByDateRange(
            ZonedDateTime.of(2024, 1, 10, 0, 0, 0, 0, ZoneOffset.UTC),
            ZonedDateTime.of(2024, 1, 20, 23, 59, 59, 999_999_999, ZoneOffset.UTC)
        );
        assertEquals("Database should contain all 3 records", 3, allSales.size());
    }

    /**
     * Test getting day sales by date range with no results.
     * Verifies empty result handling and date parsing.
     */
    @Test
    public void testGetDaySalesByDateRange_EmptyResult() {
        // Given: No day sales data in database for the date range
        
        // When: Get day sales by date range with no data
        List<DaySalesResponse> result = reportDto.getDaySalesByDateRange("2024-02-01", "2024-02-05");

        // Then: Verify empty result
        assertNotNull("Result should not be null", result);
        assertTrue("Result should be empty", result.isEmpty());
        
        // Verify database state - should be empty for this range
        List<DaySalesPojo> dbResult = reportDao.getDaySalesByDateRange(
            ZonedDateTime.of(2024, 2, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            ZonedDateTime.of(2024, 2, 5, 23, 59, 59, 999_999_999, ZoneOffset.UTC)
        );
        assertTrue("Database should be empty for this range", dbResult.isEmpty());
    }

    /**
     * Test getting day sales by single date range.
     * Verifies that same start and end date works correctly.
     */
    @Test
    public void testGetDaySalesByDateRange_SingleDate() {
        // Given: Create and persist day sales for specific date
        ZonedDateTime targetDate = ZonedDateTime.of(2024, 1, 20, 12, 0, 0, 0, ZoneOffset.UTC);
        DaySalesPojo daySales = TestData.daySales(targetDate, 10, 25, 3000.0);
        reportDao.insert(daySales);

        // When: Get day sales for single date (same start and end)
        List<DaySalesResponse> result = reportDto.getDaySalesByDateRange("2024-01-20", "2024-01-20");

        // Then: Verify single result
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 1 day sales record", 1, result.size());
        
        DaySalesResponse response = result.get(0);
        assertEquals("Orders count should match", Integer.valueOf(10), response.getInvoicedOrdersCount());
        assertEquals("Items count should match", Integer.valueOf(25), response.getInvoicedItemsCount());
        assertEquals("Revenue should match", Double.valueOf(3000.0), response.getTotalRevenue());
        
        // Verify database state
        DaySalesPojo dbRecord = reportDao.getDaySalesByDate(targetDate);
        assertNotNull("Database record should exist", dbRecord);
        assertEquals("Database revenue should match", Double.valueOf(3000.0), dbRecord.getTotalRevenue());
    }

    /**
     * Test getting day sales with edge case dates.
     * Verifies date parsing and range calculation with month boundaries.
     */
    @Test
    public void testGetDaySalesByDateRange_MonthBoundary() {
        // Given: Create day sales at month boundary
        ZonedDateTime endOfMonth = ZonedDateTime.of(2024, 1, 31, 23, 30, 0, 0, ZoneOffset.UTC);
        ZonedDateTime startOfMonth = ZonedDateTime.of(2024, 2, 1, 1, 0, 0, 0, ZoneOffset.UTC);
        
        DaySalesPojo salesJan = TestData.daySales(endOfMonth, 5, 12, 1200.0);
        DaySalesPojo salesFeb = TestData.daySales(startOfMonth, 7, 18, 1800.0);
        
        reportDao.insert(salesJan);
        reportDao.insert(salesFeb);

        // When: Get day sales across month boundary
        List<DaySalesResponse> result = reportDto.getDaySalesByDateRange("2024-01-31", "2024-02-01");

        // Then: Verify both records are returned
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 2 day sales records", 2, result.size());
        
        // Verify total revenue calculation
        double totalRevenue = result.stream()
            .mapToDouble(DaySalesResponse::getTotalRevenue)
            .sum();
        assertEquals("Total revenue should be sum of both days", 3000.0, totalRevenue, 0.001);
        
        // Verify database state
        List<DaySalesPojo> dbResult = reportDao.getDaySalesByDateRange(
            ZonedDateTime.of(2024, 1, 31, 0, 0, 0, 0, ZoneOffset.UTC),
            ZonedDateTime.of(2024, 2, 1, 23, 59, 59, 999_999_999, ZoneOffset.UTC)
        );
        assertEquals("Database should contain both records", 2, dbResult.size());
    }

    /**
     * Test getting day sales with multiple records on same date.
     * Verifies that multiple sales records for same date are handled correctly.
     */
    @Test
    public void testGetDaySalesByDateRange_MultipleRecordsSameDate() {
        // Given: Create multiple day sales for same date (different times)
        ZonedDateTime baseDate = ZonedDateTime.of(2024, 1, 25, 0, 0, 0, 0, ZoneOffset.UTC);
        ZonedDateTime morning = baseDate.withHour(9);
        ZonedDateTime afternoon = baseDate.withHour(15);
        
        DaySalesPojo morningSales = TestData.daySales(morning, 3, 8, 800.0);
        DaySalesPojo afternoonSales = TestData.daySales(afternoon, 5, 12, 1200.0);
        
        reportDao.insert(morningSales);
        reportDao.insert(afternoonSales);

        // When: Get day sales for the date
        List<DaySalesResponse> result = reportDto.getDaySalesByDateRange("2024-01-25", "2024-01-25");

        // Then: Verify both records are returned
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 2 day sales records", 2, result.size());
        
        // Verify total counts and revenue
        int totalOrders = result.stream()
            .mapToInt(DaySalesResponse::getInvoicedOrdersCount)
            .sum();
        int totalItems = result.stream()
            .mapToInt(DaySalesResponse::getInvoicedItemsCount)
            .sum();
        double totalRevenue = result.stream()
            .mapToDouble(DaySalesResponse::getTotalRevenue)
            .sum();
        
        assertEquals("Total orders should be sum of both records", 8, totalOrders);
        assertEquals("Total items should be sum of both records", 20, totalItems);
        assertEquals("Total revenue should be sum of both records", 2000.0, totalRevenue, 0.001);
        
        // Verify database state
        List<DaySalesPojo> dbResult = reportDao.getDaySalesByDateRange(morning, afternoon);
        assertEquals("Database should contain both records", 2, dbResult.size());
    }

    /**
     * Test date parsing with various formats.
     * Verifies that the DTO correctly parses yyyy-MM-dd format.
     */
    @Test
    public void testGetDaySalesByDateRange_DateParsing() {
        // Given: Create day sales with specific date
        ZonedDateTime specificDate = ZonedDateTime.of(2024, 3, 5, 10, 30, 0, 0, ZoneOffset.UTC);
        DaySalesPojo daySales = TestData.daySales(specificDate, 6, 15, 1500.0);
        reportDao.insert(daySales);

        // When: Get day sales using string date format
        List<DaySalesResponse> result = reportDto.getDaySalesByDateRange("2024-03-05", "2024-03-05");

        // Then: Verify date parsing worked correctly
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 1 day sales record", 1, result.size());
        
        DaySalesResponse response = result.get(0);
        assertNotNull("Response date should not be null", response.getDate());
        assertEquals("Response date should match", specificDate, response.getDate());
        
        // Verify database state
        DaySalesPojo dbRecord = reportDao.getDaySalesByDate(specificDate);
        assertNotNull("Database record should exist", dbRecord);
        assertEquals("Database date should match", specificDate, dbRecord.getDate());
    }

    /**
     * Test response conversion functionality.
     * Verifies that POJO to Response conversion works correctly.
     */
    @Test
    public void testGetDaySalesByDateRange_ResponseConversion() {
        // Given: Create day sales with specific values
        ZonedDateTime testDate = ZonedDateTime.of(2024, 4, 10, 14, 0, 0, 0, ZoneOffset.UTC);
        DaySalesPojo daySales = TestData.daySales(testDate, 15, 45, 4500.0);
        reportDao.insert(daySales);

        // When: Get day sales through DTO
        List<DaySalesResponse> result = reportDto.getDaySalesByDateRange("2024-04-10", "2024-04-10");

        // Then: Verify response conversion
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 1 day sales record", 1, result.size());
        
        DaySalesResponse response = result.get(0);
        assertNotNull("Response should not be null", response);
        assertEquals("Date should be converted correctly", testDate, response.getDate());
        assertEquals("Orders count should be converted correctly", Integer.valueOf(15), response.getInvoicedOrdersCount());
        assertEquals("Items count should be converted correctly", Integer.valueOf(45), response.getInvoicedItemsCount());
        assertEquals("Revenue should be converted correctly", Double.valueOf(4500.0), response.getTotalRevenue());
        
        // Verify original data in database is unchanged
        DaySalesPojo dbRecord = reportDao.getDaySalesByDate(testDate);
        assertNotNull("Database record should exist", dbRecord);
        assertEquals("Database orders count should match", Integer.valueOf(15), dbRecord.getInvoicedOrdersCount());
        assertEquals("Database items count should match", Integer.valueOf(45), dbRecord.getInvoicedItemsCount());
        assertEquals("Database revenue should match", Double.valueOf(4500.0), dbRecord.getTotalRevenue());
    }

    /**
     * Test getting day sales with large date range.
     * Verifies performance and correctness with multiple records.
     */
    @Test
    public void testGetDaySalesByDateRange_LargeDateRange() {
        // Given: Create day sales for multiple days
        ZonedDateTime baseDate = ZonedDateTime.of(2024, 5, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        for (int i = 0; i < 10; i++) {
            ZonedDateTime date = baseDate.plusDays(i);
            DaySalesPojo daySales = TestData.daySales(date, i + 1, (i + 1) * 3, (i + 1) * 300.0);
            reportDao.insert(daySales);
        }

        // When: Get day sales for large date range
        List<DaySalesResponse> result = reportDto.getDaySalesByDateRange("2024-05-01", "2024-05-10");

        // Then: Verify all records are returned
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 10 day sales records", 10, result.size());
        
        // Verify total calculations
        double totalRevenue = result.stream()
            .mapToDouble(DaySalesResponse::getTotalRevenue)
            .sum();
        assertEquals("Total revenue should be sum of all days", 16500.0, totalRevenue, 0.001);
        
        // Verify database state
        List<DaySalesPojo> dbResult = reportDao.getDaySalesByDateRange(
            ZonedDateTime.of(2024, 5, 1, 0, 0, 0, 0, ZoneOffset.UTC),
            ZonedDateTime.of(2024, 5, 10, 23, 59, 59, 999_999_999, ZoneOffset.UTC)
        );
        assertEquals("Database should contain all 10 records", 10, dbResult.size());
    }

    /**
     * Test getting day sales with zero values.
     * Verifies that zero revenue and counts are handled correctly.
     */
    @Test
    public void testGetDaySalesByDateRange_ZeroValues() {
        // Given: Create day sales with zero values
        ZonedDateTime testDate = ZonedDateTime.of(2024, 6, 15, 10, 0, 0, 0, ZoneOffset.UTC);
        DaySalesPojo daySales = TestData.daySales(testDate, 0, 0, 0.0);
        reportDao.insert(daySales);

        // When: Get day sales with zero values
        List<DaySalesResponse> result = reportDto.getDaySalesByDateRange("2024-06-15", "2024-06-15");

        // Then: Verify zero values are handled correctly
        assertNotNull("Result should not be null", result);
        assertEquals("Should return 1 day sales record", 1, result.size());
        
        DaySalesResponse response = result.get(0);
        assertEquals("Orders count should be zero", Integer.valueOf(0), response.getInvoicedOrdersCount());
        assertEquals("Items count should be zero", Integer.valueOf(0), response.getInvoicedItemsCount());
        assertEquals("Revenue should be zero", Double.valueOf(0.0), response.getTotalRevenue());
        
        // Verify database state
        DaySalesPojo dbRecord = reportDao.getDaySalesByDate(testDate);
        assertNotNull("Database record should exist", dbRecord);
        assertEquals("Database revenue should be zero", Double.valueOf(0.0), dbRecord.getTotalRevenue());
    }
} 