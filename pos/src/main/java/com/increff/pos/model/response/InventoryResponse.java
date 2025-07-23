package com.increff.pos.model.response;

import com.increff.pos.model.form.InventoryForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class InventoryResponse {
    private Integer productId;
    private Integer quantity;
    private Integer id;
}
