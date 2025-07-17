package com.increff.pos.dao;

import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import com.increff.pos.model.enums.ErrorType;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import javax.transaction.Transactional;

@Repository
@Transactional
public class ProductDao extends AbstractDao<ProductPojo> {

    public ProductDao() {
        super(ProductPojo.class);
    }

    public ProductPojo selectByBarcode(String barcode) {
       return selectByField("barcode", barcode);
    }

    public List<ProductPojo> selectByBarcodes(Set<String> barcodes) {
        return selectByFieldValues("barcode", barcodes, null, SortOrder.ASC);
    }

    public List<ProductPojo> findBySearchCriteria(String barcode, String productName, int page, int size) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
        Root<ProductPojo> root = query.from(ProductPojo.class);
        List<Predicate> predicates = new ArrayList<>();

        // Add search predicates
        if (!Objects.isNull(barcode) && !barcode.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("barcode")), barcode + "%"));
        }
        if (!Objects.isNull(productName) && !productName.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), productName + "%"));
        }

        // Apply predicates if any
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        // Order by name
        query.orderBy(cb.asc(root.get("name")));

        // Apply pagination
        return entityManager.createQuery(query)
                .setFirstResult(page * size)  // Offset
                .setMaxResults(size)          // Limit
                .getResultList();
    }

    public void bulkInsert(List<ProductPojo> products) {
        // Get the underlying JDBC connection for batch processing
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            String sql = "INSERT INTO product (barcode, client_id, name, mrp, image_url) VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                for (ProductPojo product : products) {
                    stmt.setString(1, product.getBarcode());
                    stmt.setInt(2, product.getClientId());
                    stmt.setString(3, product.getName());
                    stmt.setDouble(4, product.getMrp());
                    stmt.setString(5, product.getImageUrl());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            } catch (SQLException e) {
                throw new ApiException(ErrorType.INTERNAL_SERVER_ERROR, "Failed inserting product data");
            }
        });
    }
}