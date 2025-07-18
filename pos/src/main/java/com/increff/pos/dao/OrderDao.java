package com.increff.pos.dao;

import com.increff.pos.entity.OrdersPojo;
import org.springframework.stereotype.Repository;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Repository
@Transactional
public class OrderDao extends AbstractDao<OrdersPojo> {

    public OrderDao() {
        super(OrdersPojo.class);
    }

    @Override
    public void insert(OrdersPojo pojo) {
        entityManager.persist(pojo);
        entityManager.flush();
        entityManager.refresh(pojo);
    }

    public List<OrdersPojo> findBySearchCriteria(ZonedDateTime startDate, ZonedDateTime endDate, Integer orderId, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrdersPojo> query = cb.createQuery(OrdersPojo.class);
        Root<OrdersPojo> root = query.from(OrdersPojo.class);
        List<Predicate> predicates = new ArrayList<>();
        if (startDate != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("time"), startDate));
        }
        if (endDate != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("time"), endDate));
        }
        if (orderId != null) {
            predicates.add(cb.equal(root.get("id"), orderId));
        }
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        query.orderBy(cb.desc(root.get("time")));
        // Apply pagination
        return entityManager.createQuery(query)
                .setFirstResult(page * size)  // Offset
                .setMaxResults(size)          // Limit
                .getResultList();
    }

    public void updateInvoicePath(Integer orderId, String invoicePath) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<OrdersPojo> update = cb.createCriteriaUpdate(OrdersPojo.class);
        Root<OrdersPojo> root = update.from(OrdersPojo.class);

        update.set(root.get("invoicePath"), invoicePath);
        update.where(cb.equal(root.get("id"), orderId));

        entityManager.createQuery(update).executeUpdate();
    }
}