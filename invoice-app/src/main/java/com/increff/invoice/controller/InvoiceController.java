package com.increff.invoice.controller;

import com.increff.invoice.dto.InvoiceDto;
import com.increff.invoice.model.form.OrderRequest;
import com.increff.invoice.model.response.InvoiceResponse;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * Controller for invoice generation endpoints
 * Provides REST API for generating invoice PDFs
 */
@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @PostMapping("/generate")
    @ApiOperation(value = "Generate invoice PDF from order details")
    public InvoiceResponse generateInvoice(
            @ApiParam(value = "Order details for invoice generation", required = true)
            @Valid @RequestBody OrderRequest orderRequest) {
        
        return invoiceDto.generateInvoice(orderRequest);
    }

    @GetMapping("/health")
    @ApiOperation(value = "Health check endpoint")
    public String healthCheck() {
        return "Invoice service is running";
    }
} 