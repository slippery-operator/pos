package com.increff.pos.dao;

import com.increff.pos.entity.DaySalesPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * DAO layer for Report entity operations
 * Handles database operations for daily sales data
 */
@Repository
public class ReportDao extends AbstractDao<DaySalesPojo> {

    public ReportDao() {
        super(DaySalesPojo.class);
    }

    public List<DaySalesPojo> getDaySalesByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        return selectByFieldRange("date", startDate, endDate, "date", SortOrder.ASC);
    }

    public DaySalesPojo getDaySalesByDate(ZonedDateTime date) {
        return selectByField("date", date);
    }
}
