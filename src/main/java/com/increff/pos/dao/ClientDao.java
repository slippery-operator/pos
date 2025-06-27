//package com.increff.pos.dao;
//
//import com.increff.pos.entity.ClientPojo;
//import org.springframework.stereotype.Repository;
//
//import javax.persistence.EntityManager;
//import javax.persistence.PersistenceContext;
//import javax.persistence.criteria.CriteriaBuilder;
//import javax.persistence.criteria.CriteriaQuery;
//import javax.persistence.criteria.Root;
//import javax.transaction.Transactional;
//import java.util.List;
//
//
//@Repository
//@Transactional
//public class ClientDao {
//
//    @PersistenceContext
//    private EntityManager entityManager;
//
////    private Session getSession() {
////        return entityManager.unwrap(Session.class);
////    }
//
//    public void insert(ClientPojo pojo) {
//        entityManager.persist(pojo);
//    }
//
//    public ClientPojo selectById(Integer id) {
//        return entityManager.find(ClientPojo.class, id);
//    }
//
//    public List<ClientPojo> selectAll() {
////        TypedQuery<ClientPojo> query = entityManager.createQuery(
////                "SELECT c FROM ClientPojo c ORDER BY c.name", ClientPojo.class);
////        return query.getResultList();
//            // 1. Get the CriteriaBuilder object from the EntityManager
//            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
//
//            // 2. Create a query object that will return ClientPojo
//            CriteriaQuery<ClientPojo> query = cb.createQuery(ClientPojo.class);
//
//            // 3. Set the FROM part of the query (like: FROM ClientPojo c)
//            Root<ClientPojo> root = query.from(ClientPojo.class);
//
//            // 4. Set the SELECT and ORDER BY clause (like: SELECT c ORDER BY c.name)
//            query.select(root).orderBy(cb.asc(root.get("name")));
//
//            // 5. Create a JPA query from the criteria query
//            return entityManager.createQuery(query).getResultList();
//
//    }
//
//    public void update(ClientPojo pojo) {
//        entityManager.merge(pojo);
//    }
//
//}

package com.increff.pos.dao;

import com.increff.pos.entity.ClientPojo;
import org.springframework.stereotype.Repository;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;
import java.util.List;

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

        query.select(root)
                .where(cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%"))
                .orderBy(cb.asc(root.get("name")));

        return entityManager.createQuery(query).getResultList();
    }
}