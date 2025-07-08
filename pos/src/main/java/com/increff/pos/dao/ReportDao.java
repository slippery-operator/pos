package com.increff.pos.dao;

import com.increff.pos.entity.DaySalesPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DaySalesPojo> query = cb.createQuery(DaySalesPojo.class);
        Root<DaySalesPojo> root = query.from(DaySalesPojo.class);

        Predicate dateGreaterThanOrEqual = cb.greaterThanOrEqualTo(root.get("date"), startDate);
        Predicate dateLessThanOrEqual = cb.lessThanOrEqualTo(root.get("date"), endDate);

        query.select(root)
                .where(cb.and(dateGreaterThanOrEqual, dateLessThanOrEqual))
                .orderBy(cb.asc(root.get("date")));

        return entityManager.createQuery(query).getResultList();
    }

    public DaySalesPojo getDaySalesByDate(LocalDate date) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<DaySalesPojo> query = cb.createQuery(DaySalesPojo.class);
        Root<DaySalesPojo> root = query.from(DaySalesPojo.class);

        query.select(root).where(cb.equal(root.get("date"), date));

        List<DaySalesPojo> results = entityManager.createQuery(query).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }
}
