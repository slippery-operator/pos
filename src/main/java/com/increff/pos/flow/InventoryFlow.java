package com.increff.pos.flow;

import com.increff.pos.api.InventoryApi;
import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.model.response.InventoryResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class InventoryFlow {

    @Autowired
    private InventoryApi inventoryApi;

    public List<InventoryResponse> processInventoryTsvUpload(List<InventoryPojo> inventoryPojos) {
        // Update inventories using method parameters instead of forms
        return inventoryPojos.stream()
                .map(pojo -> inventoryApi.updateInventoryByProductId(pojo.getProductId(), pojo.getQuantity()))
                .collect(Collectors.toList());
    }
}