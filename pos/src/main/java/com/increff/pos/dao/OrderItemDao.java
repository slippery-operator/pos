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
        return selectByFieldOrdered("orderId", orderId, null, SortOrder.ASC);
    }

    public void insertGroup(List<OrderItemsPojo> orderItems) {
        for (int i = 0; i < orderItems.size(); i++) {
            entityManager.persist(orderItems.get(i));
            if (i % 50 == 0) { // Batch processing
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
}