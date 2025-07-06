package com.increff.pos.dao;

import com.increff.pos.entity.InventoryPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Repository
public class InventoryDao extends AbstractDao<InventoryPojo> {

    public InventoryDao() {
        super(InventoryPojo.class);
    }

    public InventoryPojo selectByProductId(Integer productId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
        Root<InventoryPojo> root = query.from(InventoryPojo.class);

        query.select(root).where(cb.equal(root.get("productId"), productId));

        List<InventoryPojo> results = entityManager.createQuery(query).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<InventoryPojo> findByProductIdOrInventoryId(Integer productId, Integer inventoryId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
        Root<InventoryPojo> root = query.from(InventoryPojo.class);

        List<Predicate> predicates = new ArrayList<>();

        if (productId != null) {
            predicates.add(cb.equal(root.get("productId"), productId));
        }

        if (inventoryId != null) {
            predicates.add(cb.equal(root.get("id"), inventoryId));
        }

        if (!predicates.isEmpty()) {
            query.where(cb.or(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(query).getResultList();
    }

    public List<InventoryPojo> findByQuantityRange(Integer minQty, Integer maxQty) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
        Root<InventoryPojo> root = query.from(InventoryPojo.class);

        List<Predicate> predicates = new ArrayList<>();

        if (minQty != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("quantity"), minQty));
        }

        if (maxQty != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("quantity"), maxQty));
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(query).getResultList();
    }

    public void bulkInsert(List<InventoryPojo> inventories) {
        for (int i = 0; i < inventories.size(); i++) {
            entityManager.persist(inventories.get(i));
            if (i % 50 == 0) { // Batch processing
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
}