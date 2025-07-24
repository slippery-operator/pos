package com.increff.pos.dto;

import com.increff.pos.api.ReportApi;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.model.response.DaySalesResponse;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.DateUtil;
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

    @Autowired
    private ConvertUtil convertUtil;

    public List<DaySalesResponse> getDaySalesByDateRange(LocalDate startDate, LocalDate endDate) {
        ZonedDateTime startDateTime = DateUtil.toStartOfDayUTC(startDate);
        ZonedDateTime endDateTime = DateUtil.toEndOfDayUTC(endDate);
        List<DaySalesPojo> daySalesList = api.getDaySalesByDateRange(startDateTime, endDateTime);
        return daySalesList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    private DaySalesResponse convertToResponse(DaySalesPojo daySales) {
        DaySalesResponse response = convertUtil.convert(daySales, DaySalesResponse.class);
        return response;
    }
} 