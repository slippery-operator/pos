package com.increff.pos.dao;

import com.increff.pos.entity.ProductPojo;
import com.increff.pos.exception.ApiException;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.transaction.Transactional;

import java.util.Set;

@Repository
@Transactional
public class ProductDao extends AbstractDao<ProductPojo> {

    public ProductDao() {
        super(ProductPojo.class);
    }

    public ProductPojo selectByBarcode(String barcode) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
        Root<ProductPojo> root = query.from(ProductPojo.class);
        query.select(root).where(cb.equal(root.get("barcode"), barcode));
        List<ProductPojo> results = entityManager.createQuery(query).getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    public List<ProductPojo> findBySearchCriteria(String barcode, String productName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
        Root<ProductPojo> root = query.from(ProductPojo.class);
        List<Predicate> predicates = new ArrayList<>();
        if (barcode != null && !barcode.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("barcode")), barcode + "%"));
        }
        if (productName != null && !productName.isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")), productName + "%"));
        }
        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }
        query.orderBy(cb.asc(root.get("name")));
        return entityManager.createQuery(query).getResultList();
    }

    public List<ProductPojo> selectByBarcodes(Set<String> barcodes) {
        if (barcodes == null || barcodes.isEmpty()) {
            return Collections.emptyList();
        }
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
        Root<ProductPojo> root = query.from(ProductPojo.class);

        query.select(root).where(root.get("barcode").in(barcodes));

        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Bulk insert products using native SQL for better performance.
     * Uses JDBC batch processing to insert multiple products efficiently.
     * 
     * @param products List of products to insert
     */
    public void bulkInsert(List<ProductPojo> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        // Get the underlying JDBC connection for batch processing
        Session session = entityManager.unwrap(Session.class);
        session.doWork(connection -> {
            String sql = "INSERT INTO product (barcode, client_id, name, mrp, image_url) VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement stmt = connection.prepareStatement(sql)) {
                connection.setAutoCommit(false);
                
                for (ProductPojo product : products) {
                    stmt.setString(1, product.getBarcode());
                    stmt.setInt(2, product.getClientId());
                    stmt.setString(3, product.getName());
                    stmt.setDouble(4, product.getMrp());
                    stmt.setString(5, product.getImageUrl());
                    stmt.addBatch();
                }
                
                // Execute batch and commit
                stmt.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new ApiException(ApiException.ErrorType.INTERNAL_SERVER_ERROR, "Error inserting tsv data");
            } finally {
                connection.setAutoCommit(true);
            }
        });
    }
}