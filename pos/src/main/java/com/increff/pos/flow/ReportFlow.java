package com.increff.pos.flow;

import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.ReportApi;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.model.DaySalesModel;
import com.increff.pos.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.ZonedDateTime;


@Service
@Transactional
public class ReportFlow {

    @Autowired
    private InvoiceApi invoiceApi;

    @Autowired
    private ReportApi reportApi;

    @Scheduled(cron = "0 59 23 * * ?", zone = "UTC")
    @Transactional
    public void calculateDailySales() {
        ZonedDateTime yesterday = DateUtil.getYesterday();
        calculateDailySalesForDate(yesterday);
    }

    @Transactional
    public void calculateDailySalesForDate(ZonedDateTime date) {
        ZonedDateTime startInstant = DateUtil.getStartOfDay(date);
        ZonedDateTime endInstant = DateUtil.getEndOfDay(date);

        DaySalesModel daySalesInfo = invoiceApi.getInvoicesDataByDateRange(startInstant, endInstant);
        DaySalesPojo existingDaySales = reportApi.getDaySalesByDate(date);
        if (existingDaySales != null) {
            // Update existing in case alr manually gen...
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