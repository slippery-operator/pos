package com.increff.pos.controller;

import com.increff.pos.dto.ProductDto;
import com.increff.pos.model.form.ProductSearchForm;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.form.ProductForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/products")
@Api
public class ProductController {

    @Autowired
    private ProductDto productDto;

    @PostMapping("/search")
    public List<ProductResponse> searchProducts(@RequestBody(required = false) ProductSearchForm searchRequest) {
        return productDto.searchProducts(searchRequest);
    }

    @PostMapping
    public ProductResponse createProduct(@Valid @RequestBody ProductForm productForm) {
        return productDto.createProduct(productForm);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ApiOperation(value = "Upload products from TSV file", consumes = "multipart/form-data")
    public List<ProductResponse> uploadProductsTsv(
            @ApiParam(value = "TSV file containing product data", required = true)
            @RequestPart(value = "file") MultipartFile file) {
        return productDto.uploadProductsTsv(file);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Integer id, @Valid @RequestBody ProductForm productForm) {
        return productDto.updateProduct(id, productForm);
    }
}