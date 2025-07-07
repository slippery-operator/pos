package com.increff.pos.dto;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.model.form.InventoryUpdateForm;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.model.form.InventoryFormWithRow;
import com.increff.pos.model.response.TsvUploadResponse;
import com.increff.pos.model.response.TsvValidationError;
import com.increff.pos.util.ConvertUtil;
import com.increff.pos.util.TsvParserUtil;
import com.increff.pos.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;

import static com.increff.pos.util.StringUtil.toLowerCase;

@Service
public class InventoryDto extends AbstractDto<InventoryForm> {

    @Autowired
    private InventoryApi inventoryApi;

    @Autowired
    private ConvertUtil convertUtil;

    public List<InventoryResponse> searchInventory(String productName) {
        List<InventoryPojo> inventoryPojos = inventoryApi.searchInventory(toLowerCase(productName));
        return convertUtil.convertList(inventoryPojos, InventoryResponse.class);
    }

    /**
     * Upload inventory from TSV file with partial failure handling and row-wise validation.
     * Processes valid rows and returns detailed error information for invalid rows.
     */
    public TsvUploadResponse<InventoryResponse> uploadInventoryTsv(MultipartFile file) {
        validationUtil.validateTsvFile(file);
        List<InventoryFormWithRow> inventoryFormsWithRow = TsvParserUtil.parseInventoryTsvWithRow(file);
        List<TsvValidationError> formValidationErrors = validationUtil.validateInventoryFormsWithRow(inventoryFormsWithRow);
        List<InventoryFormWithRow> validForms = new ArrayList<>();
        for (InventoryFormWithRow inventoryWithRow : inventoryFormsWithRow) {
            boolean hasError = formValidationErrors.stream().anyMatch(error -> error.getRowNumber() == inventoryWithRow.getRowNumber());
            if (!hasError) {
                validForms.add(inventoryWithRow);
            }
        }
        List<InventoryPojo> validInventoryPojos = new ArrayList<>();
        for (InventoryFormWithRow validForm : validForms) {
            validInventoryPojos.add(convertUtil.convert(validForm.getForm(), InventoryPojo.class));
        }
        InventoryApi.InventoryTsvUploadResult apiResult = inventoryApi.bulkCreateOrUpdateInventoryWithResult(validInventoryPojos, validForms);
        List<InventoryResponse> successfulResponses = convertUtil.convertList(apiResult.getSuccessfulItems(), InventoryResponse.class);
        List<TsvValidationError> allErrors = new ArrayList<>();
        allErrors.addAll(formValidationErrors);
        allErrors.addAll(apiResult.getApiErrors());
        return ResponseUtil.buildTsvUploadResponse(inventoryFormsWithRow, successfulResponses, allErrors);
    }

    public InventoryResponse updateInventoryByProductId(Integer productId, InventoryUpdateForm inventoryUpdateForm) {
        validateId(productId, "product Id");
        validateForm(inventoryUpdateForm);
        InventoryPojo updated = inventoryApi.updateInventoryByProductId(productId, inventoryUpdateForm.getQuantity());
        return convertUtil.convert(updated, InventoryResponse.class);
    }
}