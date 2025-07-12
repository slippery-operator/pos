package com.increff.pos.controller;

import com.increff.pos.dto.InventoryDto;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto dto;

    @GetMapping
    public List<InventoryResponse> searchInventory(
            @RequestParam(required = false) String productName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return dto.searchInventory(productName, page, size);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadInventory(@RequestPart(value = "file") MultipartFile file) {
        return dto.uploadInventory(file);
    }

    @PutMapping("/{product-id}")
    public InventoryResponse updateInventoryByProductId(
            @PathVariable("product-id") Integer productId,
            @Valid @RequestBody InventoryUpdateForm inventoryUpdateForm) {
        return dto.updateInventoryByProductId(productId, inventoryUpdateForm);
    }
}