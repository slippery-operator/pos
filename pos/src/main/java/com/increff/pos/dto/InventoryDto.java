package com.increff.pos.dto;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryDto extends AbstractDto<InventoryForm> {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ConvertUtil convertUtil;

    public List<InventoryResponse> searchInventory(Integer minQty, Integer maxQty) {
        validationUtil.validateQuantityRange(minQty, maxQty);
        return inventoryApi.searchInventory(minQty, maxQty);
    }

    public List<InventoryResponse> uploadInventoryTsv(MultipartFile file) {
        validationUtil.validateTsvFile(file);

        // Parse and validate TSV file to InventoryForm list
        List<InventoryForm> inventoryForms = TsvParserUtil.parseInventoryTsv(file);

        // Validate all forms
        validationUtil.validateForms(inventoryForms);

        // Convert forms to POJOs using ConvertUtil
        List<InventoryPojo> inventoryPojos = convertUtil.convertList(inventoryForms, InventoryPojo.class);

        // Direct API call instead of using flow layer
        return inventoryPojos.stream()
                .map(pojo -> inventoryApi.updateInventoryByProductId(pojo.getProductId(), pojo.getQuantity()))
                .collect(Collectors.toList());
    }

    public InventoryResponse updateInventoryByProductId(Integer productId, InventoryUpdateForm inventoryUpdateForm) {
        validateId(productId, "product Id");
        validateInventoryUpdateForm(inventoryUpdateForm);
        return inventoryApi.updateInventoryByProductId(productId, inventoryUpdateForm.getQuantity());
    }

    @Override
    protected void validateForm(InventoryForm form) {
        validationUtil.validateForm(form);
    }

    private void validateInventoryUpdateForm(InventoryUpdateForm form) {
        validationUtil.validateForm(form);
    }
}