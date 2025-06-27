package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.DuplicateEntityException;
import com.increff.pos.exception.EntityNotFoundException;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.model.form.ProductSearchForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductApi {

    @Autowired
    private ProductDao productDao;

    public List<ProductResponse> searchProducts(ProductSearchForm searchRequest) {
        List<ProductPojo> products = productDao.findBySearchCriteria(searchRequest);
        return products.stream()
                .map(this::convertToProductData)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Integer id) {
        ProductPojo product = productDao.selectById(id);
        if (product == null) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        return convertToProductData(product);
    }

    public ProductResponse createProduct(ProductForm productForm) {
        ProductPojo product = convertToProduct(productForm);
        productDao.insert(product);
        return convertToProductData(product);
    }

    public ProductResponse updateProduct(Integer id, ProductForm productForm) {
        ProductPojo existingProduct = productDao.selectById(id);
        if (existingProduct == null) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }

        updateProductFromForm(existingProduct, productForm);
        productDao.update(existingProduct);
        return convertToProductData(existingProduct);
    }

    public void validateBarcodeUniqueness(String barcode, Integer excludeId) {
        ProductPojo existingProduct = productDao.selectByBarcode(barcode);
        if (existingProduct != null && !existingProduct.getId().equals(excludeId)) {
            throw new DuplicateEntityException("Product with barcode " + barcode + " already exists");
        }
    }

    public void validateBarcodesUniqueness(List<String> barcodes) {
        Set<String> uniqueBarcodes = barcodes.stream().collect(Collectors.toSet());
        if (uniqueBarcodes.size() != barcodes.size()) {
            throw new DuplicateEntityException("Duplicate barcodes found in upload data");
        }

        List<ProductPojo> existingProducts = productDao.selectByBarcodes(uniqueBarcodes);
        if (!existingProducts.isEmpty()) {
            String conflictingBarcodes = existingProducts.stream()
                    .map(ProductPojo::getBarcode)
                    .collect(Collectors.joining(", "));
            throw new DuplicateEntityException("Products already exist with barcodes: " + conflictingBarcodes);
        }
    }

    public List<ProductResponse> bulkCreateProducts(List<ProductForm> productForms) {
        List<ProductPojo> products = productForms.stream()
                .map(this::convertToProduct)
                .collect(Collectors.toList());

        productDao.bulkInsert(products);

        return products.stream()
                .map(this::convertToProductData)
                .collect(Collectors.toList());
    }

    private ProductPojo convertToProduct(ProductForm form) {
        ProductPojo product = new ProductPojo();
        product.setBarcode(form.getBarcode());
        product.setClientId(form.getClientId());
        product.setName(form.getName());
        product.setMrp(form.getMrp());
        product.setImageUrl(form.getImageUrl());
        return product;
    }

    private ProductResponse convertToProductData(ProductPojo product) {
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

    private void updateProductFromForm(ProductPojo product, ProductForm form) {
        product.setBarcode(form.getBarcode());
        product.setClientId(form.getClientId());
        product.setName(form.getName());
        product.setMrp(form.getMrp());
        product.setImageUrl(form.getImageUrl());
    }
}