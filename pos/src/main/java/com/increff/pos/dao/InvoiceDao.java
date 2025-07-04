package com.increff.pos.dao;

import com.increff.pos.entity.InvoicePojo;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.time.Instant;
import java.util.List;

/**
 * DAO layer for Invoice entity in POS app
 * Handles database operations for invoice table using CriteriaBuilder
 */
@Repository
public class InvoiceDao extends AbstractDao<InvoicePojo> {

    public InvoiceDao() {
        super(InvoicePojo.class);
    }

    /**
     * Get invoice by order ID using CriteriaBuilder
     * Provides type-safe query construction
     */
    public InvoicePojo selectByOrderId(Integer orderId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InvoicePojo> cq = cb.createQuery(InvoicePojo.class);
        Root<InvoicePojo> root = cq.from(InvoicePojo.class);
        
        // Build predicate for order ID match
        Predicate orderIdPredicate = cb.equal(root.get("orderId"), orderId);
        cq.where(orderIdPredicate);
        
        TypedQuery<InvoicePojo> query = entityManager.createQuery(cq);
        List<InvoicePojo> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Get invoice by invoice number using CriteriaBuilder
     * Provides type-safe query construction
     */
    public InvoicePojo selectByInvoiceNumber(String invoiceNumber) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InvoicePojo> cq = cb.createQuery(InvoicePojo.class);
        Root<InvoicePojo> root = cq.from(InvoicePojo.class);
        
        // Build predicate for invoice number match
        Predicate invoiceNumberPredicate = cb.equal(root.get("invoiceNumber"), invoiceNumber);
        cq.where(invoiceNumberPredicate);
        
        TypedQuery<InvoicePojo> query = entityManager.createQuery(cq);
        List<InvoicePojo> results = query.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * Get all invoices ordered by timestamp descending using CriteriaBuilder
     * Provides type-safe query construction with ordering
     */
    @Override
    public List<InvoicePojo> selectAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InvoicePojo> cq = cb.createQuery(InvoicePojo.class);
        Root<InvoicePojo> root = cq.from(InvoicePojo.class);
        
        // Order by timestamp descending
        cq.orderBy(cb.desc(root.get("timeStamp")));
        
        TypedQuery<InvoicePojo> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    /**
     * Get invoices by date range using CriteriaBuilder
     * Provides type-safe query construction with date range filtering
     */
    public List<InvoicePojo> selectByDateRange(Instant startDate, Instant endDate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InvoicePojo> cq = cb.createQuery(InvoicePojo.class);
        Root<InvoicePojo> root = cq.from(InvoicePojo.class);
        
        // Build predicates for date range
        Predicate startDatePredicate = cb.greaterThanOrEqualTo(root.get("timeStamp"), startDate);
        Predicate endDatePredicate = cb.lessThanOrEqualTo(root.get("timeStamp"), endDate);
        
        // Combine predicates with AND
        Predicate dateRangePredicate = cb.and(startDatePredicate, endDatePredicate);
        cq.where(dateRangePredicate);
        
        // Order by timestamp descending
        cq.orderBy(cb.desc(root.get("timeStamp")));
        
        TypedQuery<InvoicePojo> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    /**
     * Insert new invoice
     * Persists the invoice entity to the database
     */
    @Override
    public void insert(InvoicePojo invoice) {
        entityManager.persist(invoice);
    }

    /**
     * Update existing invoice
     * Merges the invoice entity with the database
     */
    @Override
    public void update(InvoicePojo invoice) {
        entityManager.merge(invoice);
    }
} 