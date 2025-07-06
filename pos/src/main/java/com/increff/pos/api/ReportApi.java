package com.increff.pos.api;

import com.increff.pos.dao.ReportDao;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * API layer for Report entity operations
 * Handles business logic and data validation for daily sales
 */
@Service
@Transactional
public class ReportApi {

    @Autowired
    private ReportDao reportDao;

    public List<DaySalesPojo> getDaySalesByDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Start date and end date are required");
        }
        if (startDate.isAfter(endDate)) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Start date cannot be after end date");
        }
        return reportDao.getDaySalesByDateRange(startDate, endDate);
    }

    public DaySalesPojo getDaySalesByDate(LocalDate date) {
        if (date == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Date is required");
        }
        return reportDao.getDaySalesByDate(date);
    }

    public void saveDaySales(DaySalesPojo daySales) {
        if (daySales == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Day sales data is required");
        }
        if (daySales.getDate() == null) {
            throw new ApiException(ApiException.ErrorType.VALIDATION_ERROR, "Date is required");
        }
        
        DaySalesPojo existing = reportDao.getDaySalesByDate(daySales.getDate());
        if (existing != null) {
            existing.setInvoicedOrdersCount(daySales.getInvoicedOrdersCount());
            existing.setInvoicedItemsCount(daySales.getInvoicedItemsCount());
            existing.setTotalRevenue(daySales.getTotalRevenue());
            reportDao.updateDaySales(existing);
        } else {
            reportDao.insertDaySales(daySales);
        }
    }
} 