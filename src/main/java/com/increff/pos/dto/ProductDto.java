package com.increff.pos.dto;

import com.increff.pos.api.ProductApi;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductSearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProductDto {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ProductFlow productFlow;

    public List<ProductResponse> searchProducts(ProductSearchForm searchRequest) {
        validateSearchRequest(searchRequest);
        return productApi.searchProducts(searchRequest);
    }

    public ProductResponse createProduct(ProductForm productForm) {
        validateProductForm(productForm);
        return productFlow.validateAndCreateProduct(productForm);
    }

    public List<ProductResponse> uploadProductsTsv(MultipartFile file) {
        validateTsvFile(file);
        return productFlow.processProductTsvUpload(file);
    }

    public ProductResponse updateProduct(Integer id, ProductForm productForm) {
        validateProductForm(productForm);
        productApi.validateBarcodeUniqueness(productForm.getBarcode(), id);
        return productApi.updateProduct(id, productForm);
    }

    private void validateSearchRequest(ProductSearchForm request) {
        if (request == null) {
            throw new RuntimeException("Search request cannot be null");
        }
        // Additional form-level validations can be added here
    }

    private void validateProductForm(ProductForm form) {
        if (form == null) {
            throw new RuntimeException("Product form cannot be null");
        }
        if (form.getBarcode() == null || form.getBarcode().trim().isEmpty()) {
            throw new RuntimeException("Barcode is required");
        }
        if (form.getClientId() == null) {
            throw new RuntimeException("Client ID is required");
        }
        if (form.getName() == null || form.getName().trim().isEmpty()) {
            throw new RuntimeException("Product name is required");
        }
        if (form.getMrp() == null || form.getMrp() <= 0) {
            throw new RuntimeException("MRP must be greater than 0");
        }
    }

    private void validateTsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("TSV file is required");
        }
        if (!file.getOriginalFilename().endsWith(".tsv")) {
            throw new RuntimeException("File must be in TSV format");
        }
    }
}