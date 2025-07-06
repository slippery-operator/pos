package com.increff.pos.dto;

import com.increff.pos.api.ReportApi;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.model.response.DaySalesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO layer for Report operations
 * Handles business logic for daily sales reporting
 */
@Service
public class ReportDto {

    @Autowired
    private ReportApi reportApi;

    public List<DaySalesResponse> getDaySalesByDateRange(LocalDate startDate, LocalDate endDate) {
        List<DaySalesPojo> daySalesList = reportApi.getDaySalesByDateRange(startDate, endDate);
        return daySalesList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private DaySalesResponse convertToResponse(DaySalesPojo daySales) {
        DaySalesResponse response = new DaySalesResponse();
        response.setDate(daySales.getDate());
        response.setInvoicedOrdersCount(daySales.getInvoicedOrdersCount());
        response.setInvoicedItemsCount(daySales.getInvoicedItemsCount());
        response.setTotalRevenue(daySales.getTotalRevenue());
        return response;
    }
} 