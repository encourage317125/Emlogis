package com.emlogis.common.services.common;

import com.emlogis.model.common.IBaseEntity;
import org.joda.time.DateTime;

import javax.annotation.PostConstruct;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.persistence.*;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Set;

import static com.emlogis.common.Constants.EMLOGIS_PERSISTENCE_UNIT_NAME;

/**
 * Created by user on 14.07.15.
 */
@TransactionAttribute(TransactionAttributeType.REQUIRED)
@TransactionManagement(TransactionManagementType.CONTAINER)
public abstract class GeneralJPARepositoryImpl<T extends IBaseEntity, KEY extends Object>
        implements GeneralJPARepository<T, KEY> {

    //        protected static final String CREATED_FIELD = "created";
//        protected static final String MODIFIED_FIELD = "updated";
//        protected static final String CHANGED_BY_FIELD = "ownedBy";
    protected static final String TENANT_FIELD = "tenantId";

    @PersistenceContext(unitName = EMLOGIS_PERSISTENCE_UNIT_NAME)
    private EntityManager entityManager;

    private CriteriaBuilder builder;
    private CriteriaQuery<T> selectCriteria;
    private CriteriaUpdate<T> updateCriteria;
    private CriteriaDelete<T> deleteCriteria;
    private Root<T> from;
    private TypedQuery<T> typedQuery;
    private Query query;


    @PostConstruct
    public void initQueryTools() {
        builder = entityManager.getCriteriaBuilder();
    }


    public abstract Class<T> getEntityClass();

    public T findBy(Predicate... restrictions) {
        selectCriteria.where(restrictions);
        typedQuery = entityManager.createQuery(selectCriteria);
        return typedQuery.getSingleResult();
    }

    public List<T> findAllBy(Predicate... restrictions) {
        selectCriteria.where(restrictions);
        typedQuery = entityManager.createQuery(selectCriteria);
        return typedQuery.getResultList();
    }

    public List<T> findAllBy(Predicate[] restrictions, Order[] orders) {
        selectCriteria.where(restrictions);
        selectCriteria.orderBy(orders);
        typedQuery = entityManager.createQuery(selectCriteria);
        return typedQuery.getResultList();
    }

    public List<T> findAllBy(Predicate[] predicates, Integer from, Integer items) {
        selectCriteria.where(predicates);
        typedQuery = entityManager.createQuery(selectCriteria);
        if (from != null) {
            typedQuery.setFirstResult(from);
        }
        if (items != null) {
            typedQuery.setMaxResults(items);
        }
        return typedQuery.getResultList();
    }

    public List<T> findAllBy(Set<Predicate> restrictions) {
        return findAllBy(restrictions.toArray(new Predicate[restrictions.size()]));
    }

    public T create(T t) {
        t.setCreated(new DateTime());
        t.setUpdated(new DateTime());
        entityManager.persist(t);
        return t;
    }

    public T find(KEY id) {
        T t = entityManager.find(getEntityClass(), id);
        if (t == null) {
            throw new NoResultException("There is no " + getEntityClass().getSimpleName() + " with id = " + id);
        }
        return t;
    }

    public T update(T t) {
        t.setUpdated(new DateTime());
        return entityManager.merge(t);
    }

    public void delete(T t) {
        T tUp = update(t);
        entityManager.remove(tUp);
    }

    public int deleteBy(Predicate... restrictions) {
        deleteCriteria.where(restrictions);
        query = entityManager.createQuery(deleteCriteria);
        return query.executeUpdate();
    }

    public void delete(KEY id) {
        T t = find(id);
        delete(t);
    }

    public void deleteSimple(){
        getEntityManager().createNativeQuery("TRUNCATE "+getEntityClass().getSimpleName()+";").executeUpdate();
    }

    public Integer deleteAll() {
        getFrom(Operation.delete);
        query = entityManager.createQuery(deleteCriteria);
        return query.executeUpdate();
    }

    public void deleteWithoutUpdate(T t) {
        this.entityManager.remove(t);
    }

    public void deleteWithoutUpdate(KEY id) {
        T t = find(id);
        deleteWithoutUpdate(t);
    }

    public List<T> findAll() {
        //todo:: rework in a generalized way
        builder = entityManager.getCriteriaBuilder();
        selectCriteria = builder.createQuery(getEntityClass());
        from = selectCriteria.from(getEntityClass());
        selectCriteria.select(from);
        typedQuery = entityManager.createQuery(selectCriteria);
        return typedQuery.getResultList();
    }


    public CriteriaBuilder getBuilder() {
        return builder;
    }

    public CriteriaQuery<T> getCriteria() {
        return selectCriteria;
    }

    /**
     * depends on operation type method initialize CriteriaQuery, CriteriaUpdate or CriteriaDelete
     *
     * @param {@link Operation} operation to be initialized
     */
    public Root<T> getFrom(Operation operation) {
        switch (operation) {
            case select: {
                if (selectCriteria == null) {
                    selectCriteria = builder.createQuery(getEntityClass());
                    from = selectCriteria.from(getEntityClass());
                }
                return from;
            }
            case delete: {
                if (deleteCriteria == null) {
                    deleteCriteria = builder.createCriteriaDelete(getEntityClass());
                    from = deleteCriteria.from(getEntityClass());
                }
                return from;
            }
            case update: {
                if (updateCriteria == null) {
                    updateCriteria = builder.createCriteriaUpdate(getEntityClass());
                    from = updateCriteria.from(getEntityClass());
                }
                return from;
            }
            default: {
                selectCriteria = builder.createQuery(getEntityClass());
                from = selectCriteria.from(getEntityClass());
                return from;
            }
        }
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }
}
