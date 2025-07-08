package com.increff.pos.flow;

import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.ReportApi;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.model.DaySalesModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static com.increff.pos.util.DateUtil.calEndInstant;
import static com.increff.pos.util.DateUtil.calStartInstant;

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
        Instant startInstant = calStartInstant(date);
        Instant endInstant = calEndInstant(date);

        // Get all invoices for the day
        DaySalesModel daySalesInfo = invoiceApi.getInvoicesDataByDateRange(startInstant, endInstant);

        // Create or update day sales record
        DaySalesPojo daySales = new DaySalesPojo();
        daySales.setDate(date);
        daySales.setInvoicedOrdersCount(daySalesInfo.getInvoicedOrdersCount());
        daySales.setInvoicedItemsCount(daySalesInfo.getInvoicedItemsCount());
        daySales.setTotalRevenue(daySalesInfo.getTotalRevenue());

        reportApi.saveDaySales(daySales);
    }
} 