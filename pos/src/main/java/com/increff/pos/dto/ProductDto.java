package com.increff.pos.dto;

import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductSearchForm;
import com.increff.pos.model.form.ProductUpdateForm;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProductDto extends AbstractDto<ProductForm> {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private ProductFlow productFlow;

    @Autowired
    private ConvertUtil convertUtil;

    public List<ProductResponse> searchProducts(ProductSearchForm searchRequest) {
        List<ProductPojo> products = productApi.searchProducts(searchRequest.getBarcode(),
                searchRequest.getClientId(),
                searchRequest.getProductName());
        return convertUtil.convertList(products, ProductResponse.class);
    }

    public ProductResponse createProduct(ProductForm productForm) {
        validateForm(productForm);
        ProductPojo productPojo = convertUtil.convert(productForm, ProductPojo.class);

        ProductPojo createdProduct = productFlow.validateAndCreateProduct(productPojo);

        return convertUtil.convert(createdProduct, ProductResponse.class);
    }

    public List<ProductResponse> uploadProductsTsv(MultipartFile file) {
        validationUtil.validateTsvFile(file);

        // Parse and validate TSV file to ProductForm list
        List<ProductForm> productForms = TsvParserUtil.parseProductTsv(file);

        // Validate all forms
        validationUtil.validateForms(productForms);

        // Convert forms to POJOs using ConvertUtil
        List<ProductPojo> productPojos = convertUtil.convertList(productForms, ProductPojo.class);

        List<ProductPojo> createdProducts = productFlow.processProductTsvUpload(productPojos);
        return convertUtil.convertList(createdProducts, ProductResponse.class);
    }

    /**
     * Updates a product using the ProductUpdateForm.
     * Only allows updating name, mrp, and imageUrl fields.
     * Barcode and clientId cannot be changed after product creation.
     */
    public ProductResponse updateProduct(Integer id, ProductUpdateForm productUpdateForm) {
        validateId(id, "product Id");
        validateUpdateForm(productUpdateForm);

        // No need to validate barcode uniqueness since barcode cannot be changed
        ProductPojo product = productApi.updateProduct(id, productUpdateForm.getName(), 
                productUpdateForm.getMrp(), productUpdateForm.getImageUrl());
        return convertUtil.convert(product, ProductResponse.class);
    }

    /**
     * Legacy method for backward compatibility.
     * @deprecated Use updateProduct(Integer id, ProductUpdateForm productUpdateForm) instead
     */
    @Deprecated
    public ProductResponse updateProduct(Integer id, ProductForm productForm) {
        validateId(id, "product Id");
        validateForm(productForm);

        productApi.validateBarcodeUniqueness(productForm.getBarcode(), id);
        ProductPojo product = productApi.updateProduct(id, productForm.getBarcode(), productForm.getClientId(),
                productForm.getName(), productForm.getMrp(), productForm.getImageUrl());
        return convertUtil.convert(product, ProductResponse.class);
    }

    @Override
    protected void validateForm(ProductForm form) {
        validationUtil.validateForm(form);
    }

    /**
     * Validates the ProductUpdateForm using the validation utility.
     * Ensures all required fields are present and valid.
     */
    protected void validateUpdateForm(ProductUpdateForm form) {
        validationUtil.validateForm(form);
    }
}