package com.increff.pos.flow;

import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.ReportApi;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.model.DaySalesModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;


@Service
public class ReportFlow {

    @Autowired
    private InvoiceApi invoiceApi;

    @Autowired
    private ReportApi reportApi;

    /**
     * Scheduled task to calculate daily sales at 23:59 UTC
     * Runs every day at 23:59 UTC to calculate sales for that day
     */
    @Scheduled(cron = "0 59 23 * * ?", zone = "UTC")
    @Transactional
    public void calculateDailySales() {
        ZonedDateTime yesterday = ZonedDateTime.now(ZoneOffset.UTC).minusDays(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        calculateDailySalesForDate(yesterday);
    }

    /**
     * Calculate daily sales for a specific date
     * @param date The date to calculate sales for
     */
    @Transactional
    public void calculateDailySalesForDate(ZonedDateTime date) {
        ZonedDateTime startInstant = date.withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime endInstant = date.withHour(23).withMinute(59).withSecond(59).withNano(999_999_999);

        // Get all invoices for the day
        DaySalesModel daySalesInfo = invoiceApi.getInvoicesDataByDateRange(startInstant, endInstant);

        // Check if day sales record already exists for this date
        DaySalesPojo existingDaySales = reportApi.getDaySalesByDate(date);
        
        if (existingDaySales != null) {
            // Update existing record
            existingDaySales.setInvoicedOrdersCount(daySalesInfo.getInvoicedOrdersCount());
            existingDaySales.setInvoicedItemsCount(daySalesInfo.getInvoicedItemsCount());
            existingDaySales.setTotalRevenue(daySalesInfo.getTotalRevenue());
            reportApi.updateDaySales(existingDaySales);
        } else {
            // Create new record
            DaySalesPojo daySales = new DaySalesPojo();
            daySales.setDate(date);
            daySales.setInvoicedOrdersCount(daySalesInfo.getInvoicedOrdersCount());
            daySales.setInvoicedItemsCount(daySalesInfo.getInvoicedItemsCount());
            daySales.setTotalRevenue(daySalesInfo.getTotalRevenue());
            reportApi.saveDaySales(daySales);
        }
    }
} 