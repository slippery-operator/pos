package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.model.response.InventoryResponse;
import com.increff.pos.model.form.InventoryForm;
import com.increff.pos.util.TsvParserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryFlow {

    @Autowired
    private InventoryApi inventoryApi;

    public List<InventoryResponse> processInventoryTsvUpload(MultipartFile file) {
        // Parse TSV file
        List<InventoryForm> inventoryForms = TsvParserUtil.parseInventoryTsv(file);

        // Update inventories
        return inventoryForms.stream()
                .map(form -> inventoryApi.updateInventoryByProductId(form.getProductId(), form))
                .collect(Collectors.toList());
    }
}