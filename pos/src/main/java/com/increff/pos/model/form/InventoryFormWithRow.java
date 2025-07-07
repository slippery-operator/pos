package com.increff.pos.model.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
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