package com.increff.pos.model.response;

import com.increff.pos.model.form.InventoryForm;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.ZonedDateTime;

@Getter
@Setter
@NoArgsConstructor
public class InventoryResponse extends InventoryForm {

    private Integer id;
    private Integer version;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
