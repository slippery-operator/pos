package com.increff.pos.dao;

import com.increff.pos.entity.OrderItemPojo;
import org.springframework.stereotype.Repository;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class OrderItemDao extends AbstractDao<OrderItemPojo> {

    public OrderItemDao() {
        super(OrderItemPojo.class);
    }

    public List<OrderItemPojo> selectByOrderId(Integer orderId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderItemPojo> query = cb.createQuery(OrderItemPojo.class);
        Root<OrderItemPojo> root = query.from(OrderItemPojo.class);

        query.select(root).where(cb.equal(root.get("orderId"), orderId));

        return entityManager.createQuery(query).getResultList();
    }

    public void bulkInsert(List<OrderItemPojo> orderItems) {
        for (int i = 0; i < orderItems.size(); i++) {
            entityManager.persist(orderItems.get(i));
            if (i % 50 == 0) { // Batch processing
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
}