package com.increff.pos.dao;

import com.increff.pos.entity.DaySalesPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.List;

/**
 * DAO layer for DaySales entity operations
 * Handles database operations for daily sales data
 * 
 * TEMPORARILY DISABLED - DaySales functionality disabled
 */
@Repository
public class DaySalesDao extends AbstractDao<DaySalesPojo> {

    public DaySalesDao() {
        super(DaySalesPojo.class);
    }

    // All methods temporarily disabled
    // TODO: Re-enable when DaySales functionality is needed
} 