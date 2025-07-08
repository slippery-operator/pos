package com.increff.pos.dto;

import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.response.TsvUploadResponse;
import com.increff.pos.model.response.TsvValidationError;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductFormWithRow;
import com.increff.pos.model.form.ProductSearchForm;
import com.increff.pos.model.form.ProductUpdateForm;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import com.increff.pos.exception.ApiException;
import com.increff.pos.util.StringUtil;
import com.increff.pos.util.ValidationUtil;
import com.increff.pos.util.ResponseUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

import static com.increff.pos.util.StringUtil.toLowerCase;

@Service
public class ProductDto extends AbstractDto<ProductForm> {

    @Autowired
    private ProductApi productApi;
    
    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ConvertUtil convertUtil;

    public List<ProductResponse> searchProducts(ProductSearchForm searchRequest) {
        List<ProductPojo> products = productApi.searchProducts(toLowerCase(searchRequest.getBarcode()), toLowerCase(searchRequest.getProductName()));
        return convertUtil.convertList(products, ProductResponse.class);
    }

    public ProductResponse createProduct(ProductForm productForm) {
        validateForm(productForm);
        ProductPojo productPojo = convertUtil.convert(productForm, ProductPojo.class);
        ProductPojo createdProduct = productFlow.validateAndCreateProduct(productPojo);
        return convertUtil.convert(createdProduct, ProductResponse.class);
    }

    /**
     * Upload products from TSV file with partial failure handling.
     * Processes valid rows and returns detailed error information for invalid rows.
     * 
     * @param file The TSV file to upload
     * @return TsvUploadResponse containing successful products and validation errors
     */
    public TsvUploadResponse<ProductResponse> uploadProductsTsv(MultipartFile file) {
        validationUtil.validateTsvFile(file);
        List<ProductFormWithRow> productFormsWithRow = TsvParserUtil.parseProductTsv(file);
        List<TsvValidationError> formValidationErrors = validationUtil.validateFormsWithRow(productFormsWithRow);
        List<ProductFormWithRow> validForms = new ArrayList<>();
        for (ProductFormWithRow productWithRow : productFormsWithRow) {
            boolean hasError = formValidationErrors.stream().anyMatch(error -> error.getRowNumber() == productWithRow.getRowNumber());
            if (!hasError) {
                validForms.add(productWithRow);
            }
        }
        ProductFlow.ProductTsvUploadResult flowResult = productFlow.processProductTsvUpload(validForms);
        List<ProductResponse> successfulResponses = convertUtil.convertList(flowResult.getSuccessfulProducts(), ProductResponse.class);
        List<TsvValidationError> allErrors = new ArrayList<>();
        allErrors.addAll(formValidationErrors);
        allErrors.addAll(flowResult.getValidationErrors());
        return ResponseUtil.buildTsvUploadResponse(productFormsWithRow, successfulResponses, allErrors);
    }

    /**
     * Updates a product using the ProductUpdateForm.
     * Only allows updating name, mrp, and imageUrl fields.
     * Barcode and clientId cannot be changed after product creation.
     */
    public ProductResponse updateProduct(Integer id, ProductUpdateForm productUpdateForm) {
        validateUpdateInput(id, productUpdateForm);

        ProductPojo product = productApi.updateProduct(id, productUpdateForm.getName(), productUpdateForm.getMrp(),
                productUpdateForm.getImageUrl());
        return convertUtil.convert(product, ProductResponse.class);
    }

    private void validateUpdateInput(Integer id, ProductUpdateForm productUpdateForm) {
        validateId(id, "Product id");
        validateForm(productUpdateForm);
    }
}