package com.increff.pos.dto;

import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.flow.ProductFlow;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductSearchForm;
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
}