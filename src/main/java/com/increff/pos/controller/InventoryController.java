package com.increff.pos.controller;

import com.increff.pos.dto.InventoryDto;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryDto inventoryDto;

    @GetMapping
    public List<InventoryResponse> searchInventory(
            @RequestParam(required = false, name = "product-id") Integer productId,
            @RequestParam(required = false, name = "inventory-id") Integer inventoryId) {
        return inventoryDto.searchInventory(productId, inventoryId);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Upload inventory from TSV file", consumes = "multipart/form-data")
    public List<InventoryResponse> uploadInventoryTsv(
            @ApiParam(value = "TSV file containing inventory data", required = true)
            @RequestPart(value = "file") MultipartFile file) {
        return inventoryDto.uploadInventoryTsv(file);
    }

    @PutMapping("/{product-id}")
    public InventoryResponse updateInventoryByProductId(
            @PathVariable("product-id") Integer productId,
            @Valid @RequestBody InventoryUpdateForm inventoryUpdateForm) {
        return inventoryDto.updateInventoryByProductId(productId, inventoryUpdateForm);
    }
}