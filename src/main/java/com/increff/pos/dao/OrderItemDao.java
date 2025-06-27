// OrderItemDao.java
package com.increff.pos.dao;


import com.increff.pos.entity.OrderItemPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.util.List;

@Repository
public class OrderItemDao extends AbstractDao<OrderItemPojo> {
    public OrderItemDao() {
        super(OrderItemPojo.class);
    }

    public List<OrderItemPojo> selectByOrderId(Integer orderId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> cq = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = cq.from(OrderItemPojo.class);

        cq.where(cb.equal(root.get("orderId"), orderId));
        TypedQuery<OrderItemPojo> query = entityManager.createQuery(cq);
        return query.getResultList();
    }

    public List<OrderItemPojo> selectByOrderIds(List<Integer> orderIds) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> cq = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = cq.from(OrderItemPojo.class);

        cq.where(root.get("orderId").in(orderIds));
        TypedQuery<OrderItemPojo> query = entityManager.createQuery(cq);
        return query.getResultList();
    }
}