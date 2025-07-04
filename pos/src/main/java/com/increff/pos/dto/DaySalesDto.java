package com.increff.pos.dto;

import com.increff.pos.api.DaySalesApi;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.model.response.DaySalesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO layer for DaySales operations
 * Handles business logic for daily sales reporting
 * 
 * TEMPORARILY DISABLED - DaySales functionality disabled
 */
@Service
public class DaySalesDto {

    @Autowired
    private DaySalesApi daySalesApi;

    // All methods temporarily disabled
    // TODO: Re-enable when DaySales functionality is needed
} 