package com.increff.pos.controller;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.model.response.DaySalesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;


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
    public List<DaySalesResponse> getDaySalesReport(@RequestParam String startDate, @RequestParam String endDate) {
        
        return reportDto.getDaySalesByDateRange(startDate, endDate);
    }
} 