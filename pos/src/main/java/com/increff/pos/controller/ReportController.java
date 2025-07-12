package com.increff.pos.controller;

import com.increff.pos.dto.ReportDto;
import com.increff.pos.flow.ReportFlow;
import com.increff.pos.model.response.DaySalesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reports")
public class ReportController {

    @Autowired
    private ReportDto dto;

    @Autowired
    private ReportFlow flow;

    @GetMapping("/day-sales")
    public List<DaySalesResponse> getDaySalesReport(@RequestParam String startDate, @RequestParam String endDate) {
//        yyyy-MM-dd
        return dto.getDaySalesByDateRange(startDate, endDate);
    }
    @PostMapping("/run-daily-sales")
    public void runDailySalesNow() {
        flow.calculateDailySales();
    }
} 