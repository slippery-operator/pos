package com.increff.pos.controller;

import com.increff.pos.dto.InvoiceDto;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * Controller for invoice operations in POS app
 * Handles invoice generation and retrieval
 * 
 * This controller follows the layering convention by delegating to DTO layer
 * for all business logic and data conversion.
 */
@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto invoiceDto;

    @GetMapping("/generate-invoice/{id}")
    public String generateInvoice( @PathVariable Integer id) {
        return invoiceDto.generateInvoice(id);
    }

    @GetMapping("/get-invoice/{id}")
    public ResponseEntity<Resource> getInvoice( @PathVariable Integer id) {
       return invoiceDto.getInvoiceFile(id);
    }
} 