package com.increff.pos.dao;

import com.increff.pos.entity.AbstractPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.transaction.Transactional;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Repository
@Transactional
public abstract class AbstractDao<T extends AbstractPojo> {

	@PersistenceContext
	protected EntityManager entityManager;
	private final Class<T> entityClass;

	protected AbstractDao(Class<T> entityClass) {
		this.entityClass = entityClass;
	}

	public void insert(T pojo) {
		entityManager.persist(pojo);
	}

	public T selectById(Integer id) {
		return entityManager.find(entityClass, id);
	}

	public List<T> selectAll(int page, int size) {
		return selectAllOrderedBy(page, size, null, SortOrder.ASC);
	}

	public void update(T pojo) {
		entityManager.merge(pojo);
	}

	public T selectByName(String name) {
		return selectByField("name", name);
	}

	public List<T> selectAllOrderedBy(int page, int size, String orderByField, SortOrder sortOrder) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		query.select(root);
		if (orderByField != null) {
			Order order = sortOrder == SortOrder.DESC ?
					cb.desc(root.get(orderByField)) :
					cb.asc(root.get(orderByField));
			query.orderBy(order);
		}
		TypedQuery<T> typedQuery = entityManager.createQuery(query);
		typedQuery.setFirstResult(page * size); // Offset
		typedQuery.setMaxResults(size);         // Limit
		return typedQuery.getResultList();
	}

	public <V> T selectByField(String fieldName, V value) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		query.select(root).where(cb.equal(root.get(fieldName), value));
		List<T> results = entityManager.createQuery(query).getResultList();
		return results.isEmpty() ? null : results.get(0);
	}
	public <V> List<T> selectByFieldOrdered(String fieldName, V value,
											String orderByField, SortOrder sortOrder) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		query.select(root).where(cb.equal(root.get(fieldName), value));
		if (orderByField != null) {
			Order order = sortOrder == SortOrder.DESC ?
					cb.desc(root.get(orderByField)) :
					cb.asc(root.get(orderByField));
			query.orderBy(order);
		}
		return entityManager.createQuery(query).getResultList();
	}

	public <V extends Comparable<V>> List<T> selectByFieldRange(String fieldName, V startValue, V endValue,
																String orderByField, SortOrder sortOrder) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		Predicate startPredicate = cb.greaterThanOrEqualTo(root.get(fieldName), startValue);
		Predicate endPredicate = cb.lessThanOrEqualTo(root.get(fieldName), endValue);
		query.select(root).where(cb.and(startPredicate, endPredicate));
		if (orderByField != null) {
			Order order = sortOrder == SortOrder.DESC ?
					cb.desc(root.get(orderByField)) :
					cb.asc(root.get(orderByField));
			query.orderBy(order);
		}
		return entityManager.createQuery(query).getResultList();
	}
	public List<T> selectByFieldLike(int page, int size, String fieldName, String searchTerm, String orderByField, SortOrder sortOrder) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);

		if (searchTerm == null || searchTerm.isEmpty()) {
			query.select(root);
		} else {
			query.select(root).where(cb.like(cb.lower(root.get(fieldName)), searchTerm.toLowerCase() + "%"));
		}

		if (orderByField != null) {
			Order order = sortOrder == SortOrder.DESC ?
					cb.desc(root.get(orderByField)) :
					cb.asc(root.get(orderByField));
			query.orderBy(order);
		}
		TypedQuery<T> typedQuery = entityManager.createQuery(query);
		typedQuery.setFirstResult(page * size); // Offset
		typedQuery.setMaxResults(size);         // Limit
		return typedQuery.getResultList();
	}

	public <V> List<T> selectByFieldValues(String fieldName, Collection<V> values,
										   String orderByField, SortOrder sortOrder) {
		if (values == null || values.isEmpty()) {
			return Collections.emptyList();
		}
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		query.select(root).where(root.get(fieldName).in(values));
		if (orderByField != null) {
			Order order = sortOrder == SortOrder.DESC ?
					cb.desc(root.get(orderByField)) :
					cb.asc(root.get(orderByField));
			query.orderBy(order);
		}
		return entityManager.createQuery(query).getResultList();
	}

	protected <R> TypedQuery<R> getQuery(String jpql, Class<R> resultClass) {
		return entityManager.createQuery(jpql, resultClass);
	}

	public enum SortOrder {
		ASC, DESC
	}
}