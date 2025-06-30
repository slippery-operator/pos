package com.increff.pos.dto;

import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ValidationException;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductSearchForm;
import com.increff.pos.util.TsvParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ProductDto {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private Validator validator;

    public List<ProductResponse> searchProducts(ProductSearchForm searchRequest) {
        List<ProductPojo> products = productApi.searchProducts(searchRequest.getBarcode(),
                searchRequest.getClientId(),
                searchRequest.getProductName());
        return products.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse createProduct(ProductForm productForm) {
        validateProductForm(productForm);
        ProductPojo productPojo = convertFormToProduct(productForm);

        ProductPojo createdProduct = productFlow.validateAndCreateProduct(productPojo);

        return convertToProductResponse(createdProduct);
    }

    public List<ProductResponse> uploadProductsTsv(MultipartFile file) {
        validateTsvFile(file);

        // Parse and validate TSV file to ProductPojo list
        List<ProductForm> productForms = TsvParserUtil.parseProductTsv(file);

        // Validate each product form
        for (ProductForm form : productForms) {
            validateProductForm(form);
        }

        // Convert forms to POJOs
        List<ProductPojo> productPojos = productForms.stream()
                .map(this::convertFormToProduct)
                .collect(Collectors.toList());

        List<ProductPojo> createdProducts = productFlow.processProductTsvUpload(productPojos);
        return createdProducts.stream()
                .map(this::convertToProductResponse)
                .collect(Collectors.toList());
    }

    public ProductResponse updateProduct(Integer id, ProductForm productForm) {
        validateProductForm(productForm);

        productApi.validateBarcodeUniqueness(productForm.getBarcode(), id);
        ProductPojo product = productApi.updateProduct(id, productForm.getBarcode(), productForm.getClientId(),
                productForm.getName(), productForm.getMrp(), productForm.getImageUrl());
        return convertToProductResponse(product);
    }

    private void validateTsvFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ValidationException("TSV file is required");
        }
        if (!file.getOriginalFilename().endsWith(".tsv")) {
            throw new ValidationException("File must be in TSV format");
        }
    }

    private void validateProductForm(ProductForm form) {
        Set<ConstraintViolation<ProductForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<ProductForm> violation : violations) {
                sb.append(violation.getMessage()).append("; ");
            }
            throw new ValidationException("Validation failed: " + sb.toString());
        }
    }

    private ProductPojo convertFormToProduct(ProductForm form) {
        ProductPojo product = new ProductPojo();
        product.setBarcode(form.getBarcode());
        product.setClientId(form.getClientId());
        product.setName(form.getName());
        product.setMrp(form.getMrp());
        product.setImageUrl(form.getImageUrl());
        return product;
    }

    private ProductResponse convertToProductResponse(ProductPojo product) {
        ProductResponse data = new ProductResponse();
        data.setId(product.getId());
        data.setBarcode(product.getBarcode());
        data.setClientId(product.getClientId());
        data.setName(product.getName());
        data.setMrp(product.getMrp());
        data.setImageUrl(product.getImageUrl());
        data.setVersion(product.getVersion());
        data.setCreatedAt(product.getCreatedAt());
        data.setUpdatedAt(product.getUpdatedAt());
        return data;
    }
}