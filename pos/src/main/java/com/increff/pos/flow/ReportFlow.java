package com.increff.pos.flow;

import com.increff.pos.api.DaySalesApi;
import com.increff.pos.api.InvoiceApi;
import com.increff.pos.api.OrderApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.entity.InvoicePojo;
import com.increff.pos.entity.OrderItemsPojo;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.model.response.DaySalesResponse;
import com.increff.pos.model.response.SalesReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

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
    private DaySalesApi daySalesApi;

    @Autowired
    private OrderApi orderApi;

    @Autowired
    private ProductApi productApi;

    // All methods temporarily disabled
    // TODO: Re-enable when DaySales functionality is needed
} 