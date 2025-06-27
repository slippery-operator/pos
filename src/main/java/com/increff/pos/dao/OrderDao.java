// OrderDao.java
package com.increff.pos.dao;

import com.increff.pos.entity.OrderPojo;
import com.increff.pos.entity.OrderPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.time.ZonedDateTime;
import java.util.List;

@Repository
public class OrderDao extends AbstractDao<OrderPojo> {
    public OrderDao() {
        super(OrderPojo.class);
    }

    public List<OrderPojo> selectByDateRange(ZonedDateTime startDate, ZonedDateTime endDate) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderPojo> cq = cb.createQuery(OrderPojo.class);
        Root<OrderPojo> root = cq.from(OrderPojo.class);

        Predicate datePredicate = cb.between(root.get("time"), startDate, endDate);
        cq.where(datePredicate).orderBy(cb.desc(root.get("time")));

        TypedQuery<OrderPojo> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}