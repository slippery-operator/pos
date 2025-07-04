package com.increff.pos.api;

import com.increff.pos.dao.DaySalesDao;
import com.increff.pos.entity.DaySalesPojo;
import com.increff.pos.exception.ApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;

/**
 * API layer for DaySales entity operations
 * Handles business logic and data validation for daily sales
 * 
 * TEMPORARILY DISABLED - DaySales functionality disabled
 */
@Service
@Transactional
public class DaySalesApi {

    @Autowired
    private DaySalesDao daySalesDao;

    // All methods temporarily disabled
    // TODO: Re-enable when DaySales functionality is needed
} 