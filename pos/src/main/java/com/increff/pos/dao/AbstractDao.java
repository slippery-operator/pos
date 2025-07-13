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

	/**
	 * Create a typed query for custom JPQL queries
	 * @param jpql JPQL query string
	 * @param resultClass result class type
	 * @return TypedQuery instance
	 */
	protected <R> TypedQuery<R> getQuery(String jpql, Class<R> resultClass) {
		return entityManager.createQuery(jpql, resultClass);
	}

//	TODO: general method for sorting:
	public enum SortOrder {
		ASC, DESC
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

	// ======================== PAGINATION METHODS ========================

	/**
	 * Select all entities with pagination support
	 * @param page page number (0-based)
	 * @param size number of items per page
	 * @param orderByField field to order by (can be null)
	 * @param sortOrder sort order (ASC or DESC)
	 * @return paginated list of entities
	 */
	public List<T> selectAllPaginated(int page, int size, String orderByField, SortOrder sortOrder) {
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

	/**
	 * Select entities by field with LIKE operator and pagination support
	 * @param fieldName field name to search on
	 * @param searchTerm search term (will be converted to lowercase with % suffix)
	 * @param orderByField field to order by (can be null)
	 * @param sortOrder sort order (ASC or DESC)
	 * @param page page number (0-based)
	 * @param size number of items per page
	 * @return paginated list of entities matching the search criteria
	 */
	public List<T> selectByFieldLikePaginated(String fieldName, String searchTerm, 
											  String orderByField, SortOrder sortOrder, 
											  int page, int size) {
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

	/**
	 * Select entities by field range with pagination support
	 * @param fieldName field name to filter on
	 * @param startValue start value of range (inclusive)
	 * @param endValue end value of range (inclusive)
	 * @param orderByField field to order by (can be null)
	 * @param sortOrder sort order (ASC or DESC)
	 * @param page page number (0-based)
	 * @param size number of items per page
	 * @return paginated list of entities within the specified range
	 */
	public <V extends Comparable<V>> List<T> selectByFieldRangePaginated(String fieldName, V startValue, V endValue,
																		 String orderByField, SortOrder sortOrder,
																		 int page, int size) {
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

		TypedQuery<T> typedQuery = entityManager.createQuery(query);
		typedQuery.setFirstResult(page * size); // Offset
		typedQuery.setMaxResults(size);         // Limit

		return typedQuery.getResultList();
	}

	/**
	 * Get total count of all entities (for pagination metadata)
	 * @return total count of entities
	 */
	public long countAll() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<T> root = query.from(entityClass);
		query.select(cb.count(root));
		return entityManager.createQuery(query).getSingleResult();
	}

	/**
	 * Get count of entities matching field LIKE criteria (for pagination metadata)
	 * @param fieldName field name to search on
	 * @param searchTerm search term (will be converted to lowercase with % suffix)
	 * @return count of entities matching the search criteria
	 */
	public long countByFieldLike(String fieldName, String searchTerm) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<T> root = query.from(entityClass);

		if (searchTerm == null || searchTerm.isEmpty()) {
			query.select(cb.count(root));
		} else {
			query.select(cb.count(root)).where(cb.like(cb.lower(root.get(fieldName)), searchTerm.toLowerCase() + "%"));
		}

		return entityManager.createQuery(query).getSingleResult();
	}

	/**
	 * Get count of entities within field range (for pagination metadata)
	 * @param fieldName field name to filter on
	 * @param startValue start value of range (inclusive)
	 * @param endValue end value of range (inclusive)
	 * @return count of entities within the specified range
	 */
	public <V extends Comparable<V>> long countByFieldRange(String fieldName, V startValue, V endValue) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> query = cb.createQuery(Long.class);
		Root<T> root = query.from(entityClass);

		Predicate startPredicate = cb.greaterThanOrEqualTo(root.get(fieldName), startValue);
		Predicate endPredicate = cb.lessThanOrEqualTo(root.get(fieldName), endValue);

		query.select(cb.count(root)).where(cb.and(startPredicate, endPredicate));

		return entityManager.createQuery(query).getSingleResult();
	}
}