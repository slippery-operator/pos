package com.increff.pos.model.form;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryFormWithRow {
    private int rowNumber;
    private InventoryForm form;

    @Override
    public String toString() {
        return "Row: " + rowNumber + ", Form: " + form;
    }
} 