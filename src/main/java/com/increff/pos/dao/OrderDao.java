package com.increff.pos.dao;

import com.increff.pos.entity.OrderPojo;
import org.springframework.stereotype.Repository;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Repository
@Transactional
public class OrderDao extends AbstractDao<OrderPojo> {

    public OrderDao() {
        super(OrderPojo.class);
    }

    public List<OrderPojo> findBySearchCriteria(ZonedDateTime startDate, ZonedDateTime endDate, Integer orderId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> query = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = query.from(OrderPojo.class);

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

        return entityManager.createQuery(query).getResultList();
    }

    public void updateInvoicePath(Integer orderId, String invoicePath) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaUpdate<OrderPojo> update = cb.createCriteriaUpdate(OrderPojo.class);
        Root<OrderPojo> root = update.from(OrderPojo.class);

        update.set(root.get("invoicePath"), invoicePath);
        update.where(cb.equal(root.get("id"), orderId));

        entityManager.createQuery(update).executeUpdate();
    }
}