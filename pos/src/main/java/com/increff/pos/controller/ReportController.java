package com.increff.pos.controller;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.model.response.DaySalesResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for reporting operations
 * Handles sales reports and day-on-day sales endpoints
 * 
 * TEMPORARILY COMMENTED OUT - DaySales functionality disabled
 */
/*
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportDto reportDto;

    @PostMapping("/sales-report")
    @ApiOperation(value = "Generate sales report with filters")
    public List<SalesReportResponse> generateSalesReport(
            @ApiParam(value = "Report filters", required = true)
            @Valid @RequestBody ReportFilterForm filterForm) {
        
        return reportDto.generateSalesReport(filterForm);
    }

    @GetMapping("/day-sales")
    @ApiOperation(value = "Get day-on-day sales report")
    public List<DaySalesResponse> getDaySalesReport(
            @ApiParam(value = "Start date (YYYY-MM-DD)", required = false)
            @RequestParam(required = false) String startDate,
            @ApiParam(value = "End date (YYYY-MM-DD)", required = false)
            @RequestParam(required = false) String endDate) {
        
        return reportDto.getDaySalesReport(startDate, endDate);
    }

    @PostMapping("/calculate-day-sales")
    @ApiOperation(value = "Manually calculate daily sales for a specific date")
    public String calculateDaySales(
            @ApiParam(value = "Date to calculate (YYYY-MM-DD), null for yesterday", required = false)
            @RequestParam(required = false) String date) {
        
        return reportDto.calculateDaySales(date);
    }
}
*/

/**
 * Controller for reporting operations
 * Handles day-on-day sales endpoints
 */
@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportDto reportDto;

    @GetMapping("/day-sales")
    @ApiOperation(value = "Get day-on-day sales report")
    public List<DaySalesResponse> getDaySalesReport(
            @ApiParam(value = "Start date (YYYY-MM-DD)", required = true)
            @RequestParam String startDate,
            @ApiParam(value = "End date (YYYY-MM-DD)", required = true)
            @RequestParam String endDate) {
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate start = LocalDate.parse(startDate, formatter);
        LocalDate end = LocalDate.parse(endDate, formatter);
        
        return reportDto.getDaySalesByDateRange(start, end);
    }
} 