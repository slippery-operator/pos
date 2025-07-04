package com.increff.pos.dao;

import com.increff.pos.entity.OrderItemsPojo;
import org.springframework.stereotype.Repository;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.List;

@Repository
@Transactional
public class OrderItemDao extends AbstractDao<OrderItemsPojo> {

    public OrderItemDao() {
        super(OrderItemsPojo.class);
    }

    public List<OrderItemsPojo> selectByOrderId(Integer orderId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<OrderItemsPojo> query = cb.createQuery(OrderItemsPojo.class);
        Root<OrderItemsPojo> root = query.from(OrderItemsPojo.class);

        query.select(root).where(cb.equal(root.get("orderId"), orderId));

        return entityManager.createQuery(query).getResultList();
    }

    public List<OrderItemsPojo> getOrderItemsByOrderId(Integer orderId) {
        return selectByOrderId(orderId);
    }

    public void delete(OrderItemsPojo orderItem) {
        entityManager.remove(orderItem);
    }

    public void bulkInsert(List<OrderItemsPojo> orderItems) {
        for (int i = 0; i < orderItems.size(); i++) {
            entityManager.persist(orderItems.get(i));
            if (i % 50 == 0) { // Batch processing
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
}