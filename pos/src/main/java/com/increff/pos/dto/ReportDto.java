package com.increff.pos.dto;

import com.increff.pos.flow.ReportFlow;
import com.increff.pos.model.form.ReportFilterForm;
import com.increff.pos.model.response.DaySalesResponse;
import com.increff.pos.model.response.SalesReportResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * DTO layer for reporting operations in POS app
 * Handles reporting requests and delegates to Flow layer
 * 
 * This DTO follows the layering convention by only calling the Flow layer,
 * which in turn orchestrates calls to various APIs and handles business logic.
 * 
 * TEMPORARILY DISABLED - DaySales functionality disabled
 */
@Service
public class ReportDto {

    @Autowired
    private ReportFlow reportFlow;

    // All methods temporarily disabled
    // TODO: Re-enable when DaySales functionality is needed
} 