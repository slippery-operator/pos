//package com.increff.pos.dao;
//
//import com.increff.pos.entity.ProductPojo;
//import com.increff.pos.model.form.ProductSearchForm;
//import org.springframework.stereotype.Repository;
//
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Predicate;
//import javax.persistence.criteria.Root;
//import java.util.ArrayList;
//import java.util.List;
//import javax.transaction.Transactional;
//
//import java.util.Set;
//
//@Repository
//@Transactional
//public class ProductDao extends AbstractDao<ProductPojo> {
//
//    public ProductDao() {
//        super(ProductPojo.class);
//    }
//
//    public ProductPojo selectByBarcode(String barcode) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
//        Root<ProductPojo> root = query.from(ProductPojo.class);
//
//        query.select(root).where(cb.equal(root.get("barcode"), barcode));
//
//        List<ProductPojo> results = entityManager.createQuery(query).getResultList();
//        return results.isEmpty() ? null : results.get(0);
//    }
//
//    public List<ProductPojo> findBySearchCriteria(ProductSearchForm searchRequest) {
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
//        Root<ProductPojo> root = query.from(ProductPojo.class);
//
//        List<Predicate> predicates = new ArrayList<>();
//
//        if (searchRequest.getId() != null) {
//            predicates.add(cb.equal(root.get("id"), searchRequest.getId()));
//        }
//
//        if (searchRequest.getBarcode() != null && !searchRequest.getBarcode().trim().isEmpty()) {
//            predicates.add(cb.equal(root.get("barcode"), searchRequest.getBarcode().trim()));
//        }
//
//        if (searchRequest.getClientId() != null) {
//            predicates.add(cb.equal(root.get("clientId"), searchRequest.getClientId()));
//        }
//
//        if (searchRequest.getProductName() != null && !searchRequest.getProductName().trim().isEmpty()) {
//            predicates.add(cb.like(cb.lower(root.get("name")),
//                    "%" + searchRequest.getProductName().trim().toLowerCase() + "%"));
//        }
//
//        if (!predicates.isEmpty()) {
//            query.where(cb.and(predicates.toArray(new Predicate[0])));
//        }
//
//        return entityManager.createQuery(query).getResultList();
//    }
//
//    public List<ProductPojo> selectByBarcodes(Set<String> barcodes) {
//        if (barcodes == null || barcodes.isEmpty()) {
//            return new ArrayList<>();
//        }
//
//        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
//        Root<ProductPojo> root = query.from(ProductPojo.class);
//
//        query.select(root).where(root.get("barcode").in(barcodes));
//
//        return entityManager.createQuery(query).getResultList();
//    }
//
//    public void bulkInsert(List<ProductPojo> products) {
//        for (int i = 0; i < products.size(); i++) {
//            entityManager.persist(products.get(i));
//            if (i % 50 == 0) { // Batch processing
//                entityManager.flush();
//                entityManager.clear();
//            }
//        }
//    }
//}
package com.increff.pos.dao;

import com.increff.pos.entity.ProductPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
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

    public List<ProductPojo> findBySearchCriteria(String barcode, Integer clientId, String productName) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
        Root<ProductPojo> root = query.from(ProductPojo.class);

        List<Predicate> predicates = new ArrayList<>();

        if (barcode != null && !barcode.trim().isEmpty()) {
            predicates.add(cb.equal(root.get("barcode"), barcode.trim()));
        }

        if (clientId != null) {
            predicates.add(cb.equal(root.get("clientId"), clientId));
        }

        if (productName != null && !productName.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("name")),
                    "%" + productName.trim().toLowerCase() + "%"));
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(query).getResultList();
    }

    public List<ProductPojo> selectByBarcodes(Set<String> barcodes) {
        if (barcodes == null || barcodes.isEmpty()) {
            return new ArrayList<>();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ProductPojo> query = cb.createQuery(ProductPojo.class);
        Root<ProductPojo> root = query.from(ProductPojo.class);

        query.select(root).where(root.get("barcode").in(barcodes));

        return entityManager.createQuery(query).getResultList();
    }

    public void bulkInsert(List<ProductPojo> products) {
        for (int i = 0; i < products.size(); i++) {
            entityManager.persist(products.get(i));
            if (i % 50 == 0) { // Batch processing
                entityManager.flush();
                entityManager.clear();
            }
        }
    }
}