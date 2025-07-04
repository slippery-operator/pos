package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.entity.ProductPojo;
import com.increff.pos.model.response.ProductResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductFlow {

    @Autowired
    private ProductApi productApi;

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ClientApi clientApi;

    public ProductPojo validateAndCreateProduct(ProductPojo productPojo) {
        // Validate client exists
        clientApi.validateClientExists(productPojo.getClientId());

        // Validate barcode uniqueness
        productApi.validateBarcodeUniqueness(productPojo.getBarcode(), null);

        // Create product
        ProductPojo createdProduct = productApi.createProduct(productPojo);

        // Create initial inventory with 0 quantity
        inventoryApi.createInventory(createdProduct.getId(), 0);

        return createdProduct;
    }

    public List<ProductPojo> processProductTsvUpload(List<ProductPojo> productPojos) {
        // Extract client IDs for validation
        Set<Integer> clientIds = productPojos.stream()
                .map(ProductPojo::getClientId)
                .collect(Collectors.toSet());

        // Validate all clients exist
        clientApi.validateClientsExist(clientIds);

        // Extract barcodes for validation
        List<String> barcodes = productPojos.stream()
                .map(ProductPojo::getBarcode)
                .collect(Collectors.toList());

        // Validate barcode uniqueness
        productApi.validateBarcodesUniqueness(barcodes);

        // Bulk create products
        List<ProductPojo> createdProducts = productApi.bulkCreateProducts(productPojos);

        // Create inventory POJOs for bulk insertion
        List<InventoryPojo> inventoryPojos = createdProducts.stream()
                .map(product -> {
                    InventoryPojo inventoryPojo = new InventoryPojo();
                    inventoryPojo.setProductId(product.getId());
                    inventoryPojo.setQuantity(0);
                    return inventoryPojo;
                })
                .collect(Collectors.toList());

        inventoryApi.bulkCreateInventory(inventoryPojos);

        return createdProducts;
    }
}