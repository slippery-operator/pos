package com.increff.pos.dao;

import com.increff.pos.entity.DaySalesPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import java.time.LocalDate;
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

    public List<DaySalesPojo> getDaySalesByDateRange(LocalDate startDate, LocalDate endDate) {
        TypedQuery<DaySalesPojo> query = entityManager.createQuery(
            "SELECT d FROM DaySalesPojo d WHERE d.date >= :startDate AND d.date <= :endDate ORDER BY d.date",
            DaySalesPojo.class);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        return query.getResultList();
    }

    public DaySalesPojo getDaySalesByDate(LocalDate date) {
        TypedQuery<DaySalesPojo> query = entityManager.createQuery(
            "SELECT d FROM DaySalesPojo d WHERE d.date = :date",
            DaySalesPojo.class);
        query.setParameter("date", date);
        List<DaySalesPojo> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public void insertDaySales(DaySalesPojo daySales) {
        entityManager.persist(daySales);
    }

    public void updateDaySales(DaySalesPojo daySales) {
        entityManager.merge(daySales);
    }
} 