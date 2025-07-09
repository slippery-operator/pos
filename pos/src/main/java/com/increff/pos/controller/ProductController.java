package com.increff.pos.controller;

import com.increff.pos.dto.ProductDto;
import com.increff.pos.model.form.ProductSearchForm;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.response.TsvUploadResponse;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductUpdateForm;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductDto dto;

    @PostMapping("/search")
    public List<ProductResponse> searchProducts(@RequestBody(required = false) ProductSearchForm searchRequest) {
        return dto.searchProducts(searchRequest);
    }

    @PostMapping
    public ProductResponse createProduct(@Valid @RequestBody ProductForm productForm) {
        return dto.createProduct(productForm);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TsvUploadResponse<ProductResponse> uploadProductsTsv(@RequestPart(value = "file") MultipartFile file) {
        return dto.uploadProductsTsv(file);
    }
    
    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductUpdateForm productUpdateForm) {
        return dto.updateProduct(id, productUpdateForm);
    }
}