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
    @ApiOperation(value = "Generate invoice PDF for order")
    public String generateInvoice(
            @ApiParam(value = "Order ID", required = true)
            @PathVariable Integer id) {
        
        // Delegate to DTO layer for business logic and validation
        return invoiceDto.generateInvoice(id);
    }

    @GetMapping("/get-invoice/{id}")
    @ApiOperation(value = "Get invoice PDF file for order")
    public ResponseEntity<Resource> getInvoice(
            @ApiParam(value = "Order ID", required = true)
            @PathVariable Integer id) {
        
        try {
            // Get invoice file path from DTO layer
            String invoicePath = invoiceDto.getInvoicePath(id);
            
            // Create file resource for serving
            File file = new File(invoicePath);
            if (!file.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(file);
            
            // Set headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "invoice_" + id + ".pdf");
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(resource);
                    
        } catch (Exception e) {
            // Return 404 if invoice not found or any other error
            return ResponseEntity.notFound().build();
        }
    }
} 