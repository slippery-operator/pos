package com.increff.pos.dao;

import com.increff.pos.entity.InvoicePojo;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class InvoiceDao extends AbstractDao<InvoicePojo> {

    public InvoiceDao() {
        super(InvoicePojo.class);
    }

    public InvoicePojo selectByOrderId(Integer orderId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InvoicePojo> cq = cb.createQuery(InvoicePojo.class);
        Root<InvoicePojo> root = cq.from(InvoicePojo.class);
        Predicate orderIdPredicate = cb.equal(root.get("orderId"), orderId);
        cq.where(orderIdPredicate);
        TypedQuery<InvoicePojo> query = entityManager.createQuery(cq);
        List<InvoicePojo> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<InvoicePojo> selectByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        return selectByFieldRange("timeStamp", startDate, endDate, "timeStamp", SortOrder.DESC);
    }
} 