package com.increff.pos.dto;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.flow.InventoryFlow;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class InventoryDto extends AbstractDto<InventoryForm> {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private InventoryFlow inventoryFlow;

    @Autowired
    private ConvertUtil convertUtil;

    public List<InventoryResponse> searchInventory(Integer productId, Integer inventoryId) {
        validationUtil.validateSearchParams(productId, inventoryId);
        return inventoryApi.searchInventory(productId, inventoryId);
    }

    public List<InventoryResponse> uploadInventoryTsv(MultipartFile file) {
        validationUtil.validateTsvFile(file);

        // Parse and validate TSV file to InventoryForm list
        List<InventoryForm> inventoryForms = TsvParserUtil.parseInventoryTsv(file);

        // Validate all forms
        validationUtil.validateForms(inventoryForms);

        // Convert forms to POJOs using ConvertUtil
        List<InventoryPojo> inventoryPojos = convertUtil.convertList(inventoryForms, InventoryPojo.class);

        return inventoryFlow.processInventoryTsvUpload(inventoryPojos);
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