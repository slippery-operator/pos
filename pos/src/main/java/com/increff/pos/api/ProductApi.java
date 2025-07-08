package com.increff.pos.api;

import com.increff.pos.dao.ProductDao;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
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

    public List<ProductPojo> searchProducts(String barcode, String productName) {
        List<ProductPojo> products = productDao.findBySearchCriteria(barcode, productName);
        return products;
    }

    public ProductPojo getProductById(Integer id) {
        ProductPojo product = productDao.selectById(id);
        if (product == null) {
            throw new ApiException(ApiException.ErrorType.NOT_FOUND, "Product not found");
        }
        return product;
    }

    public ProductPojo createProduct(ProductPojo product) {
        productDao.insert(product);
        return product;
    }

    /**
     * Updates a product with the provided information.
     * Only allows updating name, mrp, and imageUrl fields.
     * Barcode and clientId cannot be changed after product creation.
     */
    public ProductPojo updateProduct(Integer id, String name, Double mrp, String imageUrl) {
        ProductPojo existingProduct = productDao.selectById(id);
        if (existingProduct == null) {
            throw new ApiException(ApiException.ErrorType.NOT_FOUND, 
                "Product not found with id: " + id);
        }
        existingProduct.setName(name);
        existingProduct.setMrp(mrp);
        existingProduct.setImageUrl(imageUrl);
        return existingProduct;
    }

    public void validateBarcodeUniqueness(String barcode, Integer excludeId) {
        ProductPojo existingProduct = productDao.selectByBarcode(barcode);
        if (existingProduct != null && !existingProduct.getId().equals(excludeId)) {
            throw new ApiException(ApiException.ErrorType.CONFLICT, 
                "Product with barcode " + barcode + " already exists");
        }
    }

    public List<ProductPojo> bulkCreateProducts(List<ProductPojo> products) {
        productDao.bulkInsert(products);
        return products;
    }

    /**
     * Validates barcode uniqueness for a set of barcodes and returns a map of barcode to validation status.
     * This method performs batch validation for better performance.
     * 
     * @param barcodes Set of barcodes to validate
     * @return Map of barcode to boolean (true if unique, false if duplicate)
     */
    public Map<String, Boolean> validateBarcodesUniquenessBatch(Set<String> barcodes) {
        Map<String, Boolean> result = new HashMap<>();
        
        if (barcodes == null || barcodes.isEmpty()) {
            return result;
        }
        
        // Get all existing products with these barcodes in one query
        List<ProductPojo> existingProducts = productDao.selectByBarcodes(barcodes);
        Set<String> existingBarcodes = existingProducts.stream()
                .map(ProductPojo::getBarcode)
                .collect(Collectors.toSet());
        
        // Create result map (true if barcode is NOT in existing barcodes, i.e., unique)
        for (String barcode : barcodes) {
            result.put(barcode, !existingBarcodes.contains(barcode));
        }
        
        return result;
    }
}