package com.increff.pos.dao;

import com.increff.pos.entity.InventoryPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import javax.persistence.Query;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Transactional
public class InventoryDao extends AbstractDao<InventoryPojo> {

    public InventoryDao() {
        super(InventoryPojo.class);
    }

    public InventoryPojo selectByProductId(Integer productId) {
        return selectByField("productId", productId);
    }

    public List<InventoryPojo> findByProductNameLike(String productName, int page, int size) {
        String sql = "SELECT i.* FROM inventory i " + "JOIN product p ON i.product_id = p.id " +
                "WHERE i.quantity > 0 ";
        if (productName != null && !productName.trim().isEmpty()) {
            sql += "WHERE LOWER(p.name) LIKE ? ";
        }
        sql += "ORDER BY p.name ASC LIMIT ? OFFSET ?";
        Query nativeQuery = entityManager.createNativeQuery(sql, InventoryPojo.class);
        int paramIndex = 1;
        if (productName != null && !productName.trim().isEmpty()) {
            nativeQuery.setParameter(paramIndex++, productName.toLowerCase() + "%");
        }
        nativeQuery.setParameter(paramIndex++, size);        // LIMIT
        nativeQuery.setParameter(paramIndex, page * size);   // OFFSE
        return nativeQuery.getResultList();
    }

    public void bulkInsert(List<Integer> productIds) {
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
        String sql = "INSERT INTO inventory (product_id, quantity) VALUES (?, 0)";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (Integer productId : productIds) {
                stmt.setInt(1, productId);
                stmt.addBatch();
            }
            stmt.executeBatch();
        } catch (SQLException e) {
            throw new ApiException(ErrorType.INTERNAL_SERVER_ERROR, "failed inserting inventory");
        }
        });
    }

    public void bulkUpsert(List<InventoryPojo> inventoryList) {
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            String sql = "INSERT INTO inventory (product_id, quantity) VALUES (?, ?) ON DUPLICATE KEY UPDATE quantity = VALUES(quantity)";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (InventoryPojo pojo : inventoryList) {
                    stmt.setInt(1, pojo.getProductId());
                    stmt.setInt(2, pojo.getQuantity());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } catch (SQLException e) {
                throw new ApiException(ErrorType.INTERNAL_SERVER_ERROR, "Error inserting/updating inventory TSV data: " + e.getMessage());
            }
        });
    }

    public Map<Integer, Boolean> validateProductsExist(List<Integer> productIds) {
        Map<Integer, Boolean> result = new HashMap<>();

        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            String sql = "SELECT id FROM product WHERE id IN (" +
                    productIds.stream().map(id -> "?").collect(Collectors.joining(",")) + ")";
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (int i = 0; i < productIds.size(); i++) {
                    stmt.setInt(i + 1, productIds.get(i));
                }
                try (ResultSet rs = stmt.executeQuery()) {
                    Set<Integer> foundIds = new HashSet<>();
                    while (rs.next()) {
                        foundIds.add(rs.getInt(1));
                    }
                    for (Integer id : productIds) {
                        result.put(id, foundIds.contains(id));
                    }
                }
            }
        });
        return result;
    }

    public List<InventoryPojo> selectByProductIds(List<Integer> productIds) {
        return selectByFieldValues("productId", productIds.stream().collect(Collectors.toSet()),
                null, SortOrder.ASC );
    }
}