package com.increff.pos.flow;

import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.ReportApi;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.entity.InvoicePojo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Flow layer for reporting operations
 * Orchestrates sales reports and day-on-day sales calculations
 * 
 * TEMPORARILY DISABLED - DaySales functionality disabled
 */
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
        LocalDate yesterday = LocalDate.now(ZoneOffset.UTC).minusDays(1);
        calculateDailySalesForDate(yesterday);
    }

    /**
     * Calculate daily sales for a specific date
     * @param date The date to calculate sales for
     */
    @Transactional
    public void calculateDailySalesForDate(LocalDate date) {
        // Get start and end of the day in UTC
        ZonedDateTime startOfDay = date.atStartOfDay(ZoneOffset.UTC);
        ZonedDateTime endOfDay = date.atTime(23, 59, 59).atZone(ZoneOffset.UTC);
        
        Instant startInstant = startOfDay.toInstant();
        Instant endInstant = endOfDay.toInstant();

        // Get all invoices for the day
        List<InvoicePojo> invoices = invoiceApi.getInvoicesByDateRange(startInstant, endInstant);
        
        // Calculate totals
        int invoicedOrdersCount = invoices.size();
        int invoicedItemsCount = invoices.stream()
                .mapToInt(InvoicePojo::getCountOfItems)
                .sum();
        double totalRevenue = invoices.stream()
                .mapToDouble(InvoicePojo::getFinalRevenue)
                .sum();

        // Create or update day sales record
        DaySalesPojo daySales = new DaySalesPojo();
        daySales.setDate(date);
        daySales.setInvoicedOrdersCount(invoicedOrdersCount);
        daySales.setInvoicedItemsCount(invoicedItemsCount);
        daySales.setTotalRevenue(totalRevenue);

        reportApi.saveDaySales(daySales);
    }
} 