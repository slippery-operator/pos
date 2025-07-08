package com.increff.invoice.controller;

import com.increff.invoice.dto.InvoiceDto;
import com.increff.invoice.model.form.OrderRequest;
import com.increff.invoice.model.response.InvoiceResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @PostMapping("/generate")
    public InvoiceResponse generateInvoice( @Valid @RequestBody OrderRequest orderRequest) {
        return invoiceDto.generateInvoice(orderRequest);
    }

    @GetMapping("/health")
    public String healthCheck() {
        return "Invoice service is running";
    }
} 