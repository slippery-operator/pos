package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductApi {

    @Autowired
    private ProductDao productDao;

    public Map<String, ProductPojo> findProductsByBarcodes(List<String> barcodes) {
        Set<String> uniqueBarcodes = barcodes.stream().collect(Collectors.toSet());
        List<ProductPojo> products = productDao.selectByBarcodes(uniqueBarcodes);

        return products.stream()
                .collect(Collectors.toMap(
                        ProductPojo::getBarcode,
                        Function.identity()
                ));
    }

    public List<ProductPojo> searchProducts(String barcode, Integer clientId, String productName) {
        List<ProductPojo> products = productDao.findBySearchCriteria(barcode, clientId, productName);
        return products;
    }

    public ProductPojo getProductById(Integer id) {
        ProductPojo product = productDao.selectById(id);
        if (product == null) {
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND, 
                "Product not found with id: " + id);
        }
        return product;
    }

    public ProductPojo createProduct(ProductPojo product) {
        productDao.insert(product);
        return product;
    }

    public ProductPojo updateProduct(Integer id, String barcode, Integer clientId, String name, Double mrp, String imageUrl) {
        ProductPojo existingProduct = productDao.selectById(id);
        if (existingProduct == null) {
            throw new ApiException(ApiException.ErrorType.ENTITY_NOT_FOUND, 
                "Product not found with id: " + id);
        }

        existingProduct.setBarcode(barcode);
        existingProduct.setClientId(clientId);
        existingProduct.setName(name);
        existingProduct.setMrp(mrp);
        existingProduct.setImageUrl(imageUrl);

        productDao.update(existingProduct);
        return existingProduct;
    }

    public void validateBarcodeUniqueness(String barcode, Integer excludeId) {
        ProductPojo existingProduct = productDao.selectByBarcode(barcode);
        if (existingProduct != null && !existingProduct.getId().equals(excludeId)) {
            throw new ApiException(ApiException.ErrorType.RESOURCE_ALREADY_EXISTS, 
                "Product with barcode " + barcode + " already exists");
        }
    }

    public void validateBarcodesUniqueness(List<String> barcodes) {
        Set<String> uniqueBarcodes = barcodes.stream().collect(Collectors.toSet());
        if (uniqueBarcodes.size() != barcodes.size()) {
            throw new ApiException(ApiException.ErrorType.RESOURCE_ALREADY_EXISTS, 
                "Duplicate barcodes found in upload data");
        }

        List<ProductPojo> existingProducts = productDao.selectByBarcodes(uniqueBarcodes);
        if (!existingProducts.isEmpty()) {
            String conflictingBarcodes = existingProducts.stream()
                    .map(ProductPojo::getBarcode)
                    .collect(Collectors.joining(", "));
            throw new ApiException(ApiException.ErrorType.RESOURCE_ALREADY_EXISTS, 
                "Products already exist with barcodes: " + conflictingBarcodes);
        }
    }

    public List<ProductPojo> bulkCreateProducts(List<ProductPojo> products) {
        productDao.bulkInsert(products);
        return products;
    }
}