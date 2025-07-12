package com.increff.pos.dto;

import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.form.*;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.response.ValidationError;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import com.increff.pos.util.TsvResponseUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.increff.pos.util.StringUtil.toLowerCase;

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

    public ResponseEntity<String> uploadProducts(MultipartFile file) {
        validationUtil.validateTsvFile(file);
        List<ProductFormWithRow> productFormsWithRow = TsvParserUtil.parseProductTsv(file);
        
        // Validate all forms and collect errors
        List<ValidationError> allErrors = new ArrayList<>();
        allErrors.addAll(validationUtil.validateProductFormsWithRow(productFormsWithRow));
        
        // Get valid forms for flow validation
        Set<Integer> invalidRowInfo = allErrors.stream()
                .map(ValidationError::getRowNumber)
                .collect(Collectors.toSet());
        // Get valid forms for API validation
        List<ProductFormWithRow> validForms = productFormsWithRow.stream()
                .filter(form -> !invalidRowInfo.contains(form.getRowNumber()))
                .collect(Collectors.toList());

        if(!validForms.isEmpty()) {
            Map<Integer, ValidationError> errorsByRowNum = flow.validateProductTsvUpload(validForms);
            allErrors.addAll(errorsByRowNum.entrySet().stream()
                    .sorted(Map.Entry.comparingByKey())
                    .map(Map.Entry::getValue)
                    .collect(Collectors.toList()));
        }
        // If no errors, save all valid products
        if (allErrors.isEmpty()) {
            List<ProductPojo> validatedProducts = validForms.stream()
                    .map(formWithRow ->
                            convertUtil.convert(formWithRow.getForm(), ProductPojo.class)) // we shud just be using inventoryforms
                    .collect(Collectors.toList());
            flow.processProductTsvUpload(validatedProducts);
        }
        return TsvResponseUtil.generateProductTsvResponse(productFormsWithRow, allErrors);
    }

    public boolean checkProductExists(String barcode) {
        return api.checkProductExists(toLowerCase(barcode));
    }

    private void validateUpdateInput(Integer id, ProductUpdateForm productUpdateForm) {
        validateId(id, "Product id");
        validateForm(productUpdateForm);
    }
}