package com.increff.pos.model.form;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Wrapper class for ProductForm that includes row number and original data
 * for better error tracking during TSV upload operations.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductFormWithRow {
    
    /**
     * The product form data
     */
    private ProductForm form;
    
    /**
     * The row number in the TSV file (1-based, excluding header)
     */
    private int rowNumber;
    
    /**
     * The original line data from the TSV file
     */
    private String originalData;
} 