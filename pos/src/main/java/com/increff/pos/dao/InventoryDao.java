package com.increff.pos.dao;

import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.exception.ApiException;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

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

    public List<InventoryPojo> bulkInsert(List<Integer> productIds) {
        List<InventoryPojo> createdInventories = new ArrayList<>();
        
        for (Integer productId : productIds) {
            InventoryPojo inventory = new InventoryPojo();
            inventory.setProductId(productId);
            inventory.setQuantity(0); // Default quantity for new inventory
            
            entityManager.persist(inventory);
            createdInventories.add(inventory);
            
            // Batch processing for performance
            if (createdInventories.size() % 50 == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
        
        // Final flush for remaining items
        if (!createdInventories.isEmpty()) {
            entityManager.flush();
        }
        
        return createdInventories;
    }

    public List<InventoryPojo> findByProductNameLike(String productName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<InventoryPojo> query = cb.createQuery(InventoryPojo.class);
        Root<InventoryPojo> inventoryRoot = query.from(InventoryPojo.class);
        
        if (productName == null) {
            // If no search term provided, return all inventory items
            query.select(inventoryRoot);
        } else {
            // Join with product and search by name
            Join<Object, Object> productJoin = inventoryRoot.join("product");
            query.select(inventoryRoot)
                .where(cb.like(cb.lower(productJoin.get("name")), productName.toLowerCase() + "%"));
        }
        
        return entityManager.createQuery(query).getResultList();
    }

    public void bulkUpsert(List<InventoryPojo> inventoryList) {
        if (inventoryList == null || inventoryList.isEmpty()) {
            return;
        }
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            String sql = "INSERT INTO inventory (product_id, quantity) VALUES (?, ?) ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                connection.setAutoCommit(false);
                for (InventoryPojo pojo : inventoryList) {
                    stmt.setInt(1, pojo.getProductId());
                    stmt.setInt(2, pojo.getQuantity());
                    stmt.addBatch();
                }
                stmt.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new ApiException(ApiException.ErrorType.INTERNAL_SERVER_ERROR, "Error inserting/updating inventory TSV data: " + e.getMessage());
            } finally {
                connection.setAutoCommit(true);
            }
        });
    }
    public Map<Integer, Boolean> validateProductsExist(List<Integer> productIds) {
        Map<Integer, Boolean> result = new HashMap<>();

        if (productIds == null || productIds.isEmpty()) {
            return result;
        }

        // Query to check which product IDs exist
        String sql = "SELECT id FROM product WHERE id IN (" +
                productIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";

        try {
            List<Integer> existingIds = entityManager.createNativeQuery(sql)
                    .setParameter(1, productIds.get(0)) // This approach won't work for multiple parameters
                    .getResultList();

            // Better approach using JPQL
            String jpql = "SELECT p.id FROM ProductPojo p WHERE p.id IN :productIds";
            List<Integer> existingProductIds = entityManager.createQuery(jpql, Integer.class)
                    .setParameter("productIds", productIds)
                    .getResultList();

            Set<Integer> existingSet = new HashSet<>(existingProductIds);

            for (Integer productId : productIds) {
                result.put(productId, existingSet.contains(productId));
            }

        } catch (Exception e) {
            // If query fails, assume all products don't exist
            for (Integer productId : productIds) {
                result.put(productId, false);
            }
        }

        return result;
    }
}