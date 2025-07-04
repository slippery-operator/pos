package com.increff.pos.controller;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.model.form.ReportFilterForm;
import com.increff.pos.model.response.DaySalesResponse;
import com.increff.pos.model.response.SalesReportResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
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