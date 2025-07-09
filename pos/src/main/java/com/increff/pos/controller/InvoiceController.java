package com.increff.pos.controller;

import com.increff.pos.dto.InvoiceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/invoice")
public class InvoiceController {

    @Autowired
    private InvoiceDto dto;

    @GetMapping("/generate-invoice/{id}")
    public String generateInvoice( @PathVariable Integer id) {
        return dto.generateInvoice(id);
    }

    @GetMapping("/get-invoice/{id}")
    public ResponseEntity<Resource> getInvoice( @PathVariable Integer id) {
       return dto.getInvoiceFile(id);
    }
} 