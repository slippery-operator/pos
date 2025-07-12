package com.increff.pos.dto;

import com.increff.pos.api.ReportApi;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.model.response.DaySalesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReportDto {

    @Autowired
    private ReportApi api;

    public List<DaySalesResponse> getDaySalesByDateRange(String startDate, String endDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        ZonedDateTime parsedStart = ZonedDateTime.of(
                java.time.LocalDate.parse(startDate, formatter).atStartOfDay(),
                ZoneOffset.UTC
        );

        ZonedDateTime parsedEnd = ZonedDateTime.of(
                java.time.LocalDate.parse(endDate, formatter).atTime(23, 59, 59, 999_999_999),
                ZoneOffset.UTC
        );
        List<DaySalesPojo> daySalesList = api.getDaySalesByDateRange(parsedStart, parsedEnd);
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