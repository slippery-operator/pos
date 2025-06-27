package com.increff.pos.flow;

import com.increff.pos.api.ClientApi;
import com.increff.pos.api.InventoryApi;
import com.increff.pos.api.ProductApi;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.response.ProductResponse;
import com.increff.pos.model.form.ProductForm;
import com.increff.pos.util.TsvParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public ProductResponse validateAndCreateProduct(ProductForm productForm) {
        // Validate client exists
        clientApi.validateClientExists(productForm.getClientId());

        // Validate barcode uniqueness
        productApi.validateBarcodeUniqueness(productForm.getBarcode(), null);

        // Create product
        ProductResponse productData = productApi.createProduct(productForm);

        // Create inventory with quantity 0
        InventoryForm inventoryForm = new InventoryForm();
        inventoryForm.setProductId(productData.getId());
        inventoryForm.setQuantity(0);
        inventoryApi.createInventory(inventoryForm);

        return productData;
    }

    public List<ProductResponse> processProductTsvUpload(MultipartFile file) {
        // Parse TSV file
        List<ProductForm> productForms = TsvParserUtil.parseProductTsv(file);
        System.out.println("Parsed productForms size: " + productForms.size());

        // Extract client IDs for validation
        Set<Integer> clientIds = productForms.stream()
                .map(ProductForm::getClientId)
                .collect(Collectors.toSet());

        // Validate all clients exist
        clientApi.validateClientsExist(clientIds);

        // Extract barcodes for validation
        List<String> barcodes = productForms.stream()
                .map(ProductForm::getBarcode)
                .collect(Collectors.toList());

        // Validate barcode uniqueness
        productApi.validateBarcodesUniqueness(barcodes);

        // Bulk create products
        List<ProductResponse> createdProducts = productApi.bulkCreateProducts(productForms);

        // Bulk create inventories with quantity 0
        List<InventoryForm> inventoryForms = createdProducts.stream()
                .map(product -> {
                    InventoryForm inventoryForm = new InventoryForm();
                    inventoryForm.setProductId(product.getId());
                    inventoryForm.setQuantity(0);
                    return inventoryForm;
                })
                .collect(Collectors.toList());

        inventoryApi.bulkCreateInventory(inventoryForms);

        return createdProducts;
    }
}