package com.increff.pos.dao;

import com.increff.pos.entity.AbstractPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
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

//	TODO: general method for sorting:

}