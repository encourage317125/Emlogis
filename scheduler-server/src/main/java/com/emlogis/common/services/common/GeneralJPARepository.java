package com.emlogis.common.services.common;

import com.emlogis.model.common.IBaseEntity;

import javax.persistence.EntityManager;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.Set;

/**
 * Created by user on 14.07.15.
 */
public interface GeneralJPARepository<T extends IBaseEntity, KEY extends Object> {

    /**
     * possible operations on CriteriaQuery {@link javax.persistence.criteria.CriteriaQuery}
     */
    public enum Operation {
        select,
        delete,
        update
    }

    Class<T> getEntityClass();

    T findBy(Predicate... restrictions);

    List<T> findAllBy(Predicate... restrictions);

    List<T> findAllBy(Predicate[] restrictions, Order[] orders);

    List<T> findAllBy(Predicate[] predicates, Integer from, Integer items);

    List<T> findAllBy(Set<Predicate> restrictions);

    T create(T t);

    T find(KEY id);

    T update(T t);

    void delete(T t);

    int deleteBy(Predicate... restrictions);

    void delete(KEY id);

    Integer deleteAll();

    void deleteWithoutUpdate(T t);

    void deleteSimple();

    void deleteWithoutUpdate(KEY id);

    List<T> findAll();

    CriteriaBuilder getBuilder();

    CriteriaQuery<T> getCriteria();

    /**
     * depends on operation type method initialize CriteriaQuery, CriteriaUpdate or CriteriaDelete
     *
     * @param {@link Operation} operation to be initialized
     */
    Root<T> getFrom(Operation operation);

    EntityManager getEntityManager();
}
