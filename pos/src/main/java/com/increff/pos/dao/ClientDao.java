package com.increff.pos.dao;

import com.increff.pos.entity.ClientPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
public class ClientDao extends AbstractDao<ClientPojo> {

    public ClientDao() {
        super(ClientPojo.class);
    }

    // Overriden to sort client by NAME
    @Override
    public List<ClientPojo> selectAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ClientPojo> query = cb.createQuery(ClientPojo.class);
        Root<ClientPojo> root = query.from(ClientPojo.class);
        query.select(root).orderBy(cb.asc(root.get("name")));
        return entityManager.createQuery(query).getResultList();
    }

    public List<ClientPojo> selectByNameContaining(String name) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ClientPojo> query = cb.createQuery(ClientPojo.class);
        Root<ClientPojo> root = query.from(ClientPojo.class);
        query.select(root).where(cb.like(cb.lower(root.get("name")), name + "%"))
                .orderBy(cb.asc(root.get("name")));
        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Select clients by a set of IDs.
     * This method is used for batch validation of client existence.
     * 
     * @param ids Set of client IDs to select
     * @return List of existing clients
     */
    public List<ClientPojo> selectByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<ClientPojo> query = cb.createQuery(ClientPojo.class);
        Root<ClientPojo> root = query.from(ClientPojo.class);
        query.select(root).where(root.get("id").in(ids));
        return entityManager.createQuery(query).getResultList();
    }
}