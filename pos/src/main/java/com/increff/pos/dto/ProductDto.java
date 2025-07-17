package com.increff.pos.dto;

import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.form.*;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.response.UploadResponse;
import com.increff.pos.model.response.ValidationError;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import com.increff.pos.util.TsvResponseUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import static com.increff.pos.util.StringUtil.toLowerCase;
import static com.increff.pos.util.StringUtil.toUpperCase;

@Service
public class ProductDto extends AbstractDto<ProductForm> {

    @Autowired
    private ProductApi api;

    @Autowired
    private ProductFlow flow;

    @Autowired
    private ConvertUtil convertUtil;

    public List<ProductResponse> searchProducts(ProductSearchForm searchRequest, int page, int size) {
        List<ProductPojo> products = api.searchProducts(toLowerCase(searchRequest.getBarcode()),
                toLowerCase(searchRequest.getProductName()), page, size);
        return convertUtil.convertList(products, ProductResponse.class);
    }

    public ProductResponse createProduct(ProductForm productForm) {
        validateForm(productForm);
        ProductPojo productToCreate = convertUtil.convert(productForm, ProductPojo.class);
        ProductPojo createdProduct = flow.validateAndCreateProduct(productToCreate);
        return convertUtil.convert(createdProduct, ProductResponse.class);
    }

    public ProductResponse updateProduct(Integer id, ProductUpdateForm productUpdateForm) {
        validateUpdateInput(id, productUpdateForm);
        ProductPojo product = api.updateProduct(id, productUpdateForm.getName(), productUpdateForm.getMrp(),
                productUpdateForm.getImageUrl());
        return convertUtil.convert(product, ProductResponse.class);
    }

    public ResponseEntity<UploadResponse> uploadProducts(MultipartFile file) {
        validationUtil.validateTsvFile(file);
        List<ProductFormWithRow> allForms = TsvParserUtil.parseProductTsv(file);
        List<ValidationError> allErrors = validateAllProductForms(allForms);
        if (allErrors.isEmpty()) {
            createProductsFromValidForms(allForms, allErrors);
        }
        String tsvContent = TsvResponseUtil.buildProductTsvContent(allForms, allErrors);
        String base64Tsv = Base64.getEncoder().encodeToString(tsvContent.getBytes(StandardCharsets.UTF_8));

        UploadResponse response = new UploadResponse();
        response.setStatus(allErrors.isEmpty() ? "success" : "error");
        response.setTsvBase64(base64Tsv);
        response.setFilename("product_upload_results.tsv");

        return ResponseEntity.ok(response);
    }

    public boolean checkProductExists(String barcode) {
        return api.checkProductExists(toUpperCase(barcode));
    }

    private void createProductsFromValidForms(List<ProductFormWithRow> allForms, List<ValidationError> allErrors) {
        List<ProductFormWithRow> validForms = filterValidProductForms(allForms, allErrors);
        List<ProductPojo> productPojos = convertToProductPojos(validForms);
        flow.processProductTsvUpload(productPojos);
    }

    private List<ValidationError> validateAllProductForms(List<ProductFormWithRow> allForms) {
        List<ValidationError> allErrors = new ArrayList<>();
        allErrors.addAll(validateProductForms(allForms));
        List<ProductFormWithRow> validForms = filterValidProductForms(allForms, allErrors);
        allErrors.addAll(validateProductWithFlow(validForms));
        return allErrors;
    }

    private List<ValidationError> validateProductForms(List<ProductFormWithRow> productForms) {
        return validationUtil.validateProductFormsWithRow(productForms);
    }

    private List<ProductFormWithRow> filterValidProductForms(List<ProductFormWithRow> allForms, List<ValidationError> allErrors) {
        Set<Integer> invalidRows = allErrors.stream()
                .map(ValidationError::getRowNumber)
                .collect(Collectors.toSet());
        return allForms.stream()
                .filter(form -> !invalidRows.contains(form.getRowNumber()))
                .collect(Collectors.toList());
    }

    private List<ValidationError> validateProductWithFlow(List<ProductFormWithRow> validForms) {
        if (validForms.isEmpty()) return Collections.emptyList();
        Map<Integer, ValidationError> errorsByRowNum = flow.validateProductTsvUpload(validForms);
        return errorsByRowNum.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(Map.Entry::getValue)
                .collect(Collectors.toList());
    }

    private List<ProductPojo> convertToProductPojos(List<ProductFormWithRow> validForms) {
        return validForms.stream()
                .map(form -> convertUtil.convert(form.getForm(), ProductPojo.class))
                .collect(Collectors.toList());
    }

    private void validateUpdateInput(Integer id, ProductUpdateForm productUpdateForm) {
        validateId(id, "Product id");
        validateForm(productUpdateForm);
    }
}