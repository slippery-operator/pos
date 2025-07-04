package com.increff.pos.model.form;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Pattern;

/**
 * Form class for sales report filters
 * Contains date range, brand, and category filters
 */
@Getter
@Setter
@NoArgsConstructor
public class ReportFilterForm {

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "Start date must be in YYYY-MM-DD format")
    private String startDate;

    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "End date must be in YYYY-MM-DD format")
    private String endDate;

    private String brand;

    private String category;
} 