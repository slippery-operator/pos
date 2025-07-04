package com.increff.pos.model.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Response class for sales report data
 * Contains aggregated sales information by category
 */
@Getter
@Setter
@NoArgsConstructor
public class SalesReportResponse {

    private String brand;
    private String category;
    private Integer totalQuantity;
    private Double totalRevenue;

    /**
     * Constructor for sales report response
     */
    public SalesReportResponse(String brand, String category, Integer totalQuantity, Double totalRevenue) {
        this.brand = brand;
        this.category = category;
        this.totalQuantity = totalQuantity;
        this.totalRevenue = totalRevenue;
    }
} 