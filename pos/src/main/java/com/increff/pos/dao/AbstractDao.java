package com.increff.pos.dao;

import com.increff.pos.entity.AbstractPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Order;
import javax.transaction.Transactional;
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

	public List<T> selectAll() {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		query.select(root);
		return entityManager.createQuery(query).getResultList();
	}

	public void update(T pojo) {
		entityManager.merge(pojo);
	}

	public T selectByName(String name) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);

		query.select(root).where(cb.equal(root.get("name"), name));

		List<T> results = entityManager.createQuery(query).getResultList();
		return results.isEmpty() ? null : results.get(0);
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

	/**
	 * Generic method for selecting all entities with sorting by a specific field.
	 * This method reduces code duplication across various DAOs that need sorting functionality.
	 * 
	 * @param sortField The field name to sort by (must be a valid field in the entity)
	 * @param ascending True for ascending order, false for descending order
	 * @return List of entities sorted by the specified field
	 */
	protected List<T> selectAllSortedBy(String sortField, boolean ascending) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		
		// Create order based on ascending/descending preference
		Order order = ascending ? cb.asc(root.get(sortField)) : cb.desc(root.get(sortField));
		
		query.select(root).orderBy(order);
		return entityManager.createQuery(query).getResultList();
	}

	/**
	 * Generic method for selecting entities by name pattern with sorting.
	 * This method reduces code duplication for name-based searches with sorting.
	 * 
	 * @param namePattern The pattern to match against the name field (will be used with LIKE)
	 * @param sortField The field name to sort by
	 * @param ascending True for ascending order, false for descending order
	 * @return List of entities matching the name pattern, sorted by the specified field
	 */
	protected List<T> selectByNamePatternSortedBy(String namePattern, String sortField, boolean ascending) {
		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		
		// Create order based on ascending/descending preference
		Order order = ascending ? cb.asc(root.get(sortField)) : cb.desc(root.get(sortField));
		
		query.select(root)
			.where(cb.like(cb.lower(root.get("name")), namePattern + "%"))
			.orderBy(order);
		
		return entityManager.createQuery(query).getResultList();
	}

	/**
	 * Generic method for selecting entities by a set of IDs.
	 * This method reduces code duplication for batch ID-based selections.
	 * 
	 * @param ids Set of IDs to select
	 * @param idFieldName The name of the ID field (e.g., "id", "clientId")
	 * @return List of entities with the specified IDs
	 */
	protected List<T> selectByIds(java.util.Set<Integer> ids, String idFieldName) {
		if (ids == null || ids.isEmpty()) {
			return java.util.Collections.emptyList();
		}

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<T> query = cb.createQuery(entityClass);
		Root<T> root = query.from(entityClass);
		query.select(root).where(root.get(idFieldName).in(ids));
		return entityManager.createQuery(query).getResultList();
	}
}