package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.HashMap;
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

    public Map<String, Integer> findProductsByBarcodes(List<String> barcodes) {
        Set<String> uniqueBarcodes = barcodes.stream().collect(Collectors.toSet());
        List<ProductPojo> products = productDao.selectByBarcodes(uniqueBarcodes);
        return products.stream().collect(Collectors.toMap(ProductPojo::getBarcode, ProductPojo::getId));
    }

    public List<ProductPojo> searchProducts(String barcode, String productName, int page, int size) {
        List<ProductPojo> products = productDao.findBySearchCriteria(barcode, productName, page, size);
        return products;
    }

    public ProductPojo getProductById(Integer id) {
        ProductPojo product = productDao.selectById(id);
        if (product == null) {
            throw new ApiException(ErrorType.NOT_FOUND, "Product with id: " + id + " not found");
        }
        return product;
    }

    public ProductPojo createProduct(ProductPojo product) {
        productDao.insert(product);
        return product;
    }

    public ProductPojo updateProduct(Integer id, String name, Double mrp, String imageUrl) {
        ProductPojo existingProduct = productDao.selectById(id);
        if (existingProduct == null) {
            throw new ApiException(ErrorType.NOT_FOUND, "Product with id: " + id + " not found");
        }
        existingProduct.setName(name);
        existingProduct.setMrp(mrp);
        existingProduct.setImageUrl(imageUrl);
        return existingProduct;
    }

    public void validateBarcodeUniqueness(String barcode, Integer excludeId) {
        ProductPojo existingProduct = productDao.selectByBarcode(barcode);
        if (existingProduct != null && !existingProduct.getId().equals(excludeId)) {
            throw new ApiException(ErrorType.CONFLICT, "Product with barcode " + barcode + " already exists");
        }
    }

    public List<ProductPojo> bulkCreateProducts(List<ProductPojo> products) {
        productDao.bulkInsert(products);
        return products;
    }

    public Map<String, Boolean> validateBarcodesUniquenessBatch(Set<String> barcodes) {
        // Get all existing products with these barcodes in one query
        List<ProductPojo> existingProducts = productDao.selectByBarcodes(barcodes);
        Set<String> existingBarcodes = existingProducts.stream()
                .map(ProductPojo::getBarcode)
                .collect(Collectors.toSet());
        // Create result map (true if barcode is NOT in existing barcodes, i.e., unique)
        Map<String, Boolean> result = barcodes.stream()
                .collect(Collectors.toMap(
                        barcode -> barcode,
                        barcode -> !existingBarcodes.contains(barcode)
                ));
        return result;
    }

    public boolean checkProductExists(String barcode) {
        ProductPojo product = productDao.selectByBarcode(barcode);
        return product != null;
    }
}