package com.emlogis.rest.resources.util;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.security.ACLProtected;
import com.emlogis.common.security.ACLUtil;
import com.emlogis.common.security.AccountACE;
import com.emlogis.common.security.AccountACL;
import com.emlogis.model.PrimaryKey;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class SimpleQueryHelper {

	private final Logger logger = LoggerFactory.getLogger(SimpleQueryHelper.class);

    public static String buildFilterClause(String filterStr) {
        return buildFilterClause(filterStr, "elts");
    }

    public static String buildFilterClause(String filterStr, String alias) {
        String[] filters = filterStr.split(SimpleQuery.FILTER_DELIMITER);
        String result = "";
        for (String filter : filters) {
            result += StringUtils.isNotEmpty(result) ? " AND " + alias + "." + filter : alias + "." + filter;
        }
        return result;
    }

    public static String createSearchFieldsClause(String searchValue, String searchFields, String alias) {
        String result = null;
        if (StringUtils.isNotBlank(searchFields) && StringUtils.isNotBlank(searchValue)) {
            String[] fieldNames = searchFields.split(",");
            String[] searchValues = searchValue.split(" ");
            for (String fieldName : fieldNames) {
                for (String value : searchValues) {
                    if (StringUtils.isNotBlank(value)) {
                        if (result == null) {
                            result = alias + "." + fieldName + " LIKE '%" + value + "%' ";
                        } else {
                            result += " OR " + alias + "." + fieldName + " LIKE '%" + value + "%' ";
                        }
                    }
                }
            }
        }
        return result;
    }

    public static String createReturnedFieldsClause(String returnedFields, String alias) {
        String result = null;
        if (StringUtils.isNotBlank(returnedFields)) {
            String[] fieldNames = returnedFields.split(",");
            for (String fieldName : fieldNames) {
                if (result == null) {
                    result = " " + alias + "." + fieldName + " ";
                } else {
                    result += ", " + alias + "." + fieldName + " ";
                }
            }
        }

        return result;
    }

    /**
	 * executeSimpleQueryWithPaging() execute a simple query with or without ACL, and return a list of matching objects
	 * @param simpleQuery
	 * @return
	 */	
	public <T> ResultSet<T> executeSimpleQueryWithPaging(EntityManager entityManager, SimpleQuery simpleQuery) {
		if (simpleQuery.getAcl() != null) {
			// query with ACLs
			return executeSimpleQueryWithPagingAndACL(entityManager, simpleQuery);
		}
		
		// query wo ACL
		// Caution !!: assumes the entity name is same as JavaClassName
		String entity = simpleQuery.getEntityClass().getSimpleName();
		
		String select = "SELECT elts ";
		String queryStr = " FROM " + entity + " elts ";
		boolean where = false;
		String tenantId = simpleQuery.getTenantId();
		if (!StringUtils.isBlank(tenantId)) {
			where = true;
			tenantId= simpleQuery.getTenantId();
			queryStr += ("WHERE (elts.primaryKey.tenantId = '" + tenantId + "') ");
		}

		if (StringUtils.isNotBlank(simpleQuery.getFilter())) {
			queryStr += (where ? "AND " : "WHERE ") + "(" + buildFilterClause(simpleQuery.getFilter()) + ") ";
		}
		
		if (!StringUtils.isBlank(simpleQuery.getOrderByField())) {
			queryStr += " ORDER BY elts." + simpleQuery.getOrderByField() +
                    (simpleQuery.isOrderAscending() ? " ASC " : " DESC");
		}
		logger.debug("Executing query: " + queryStr);
        ResultSet<T> resultSet = new ResultSet<>();
		if (simpleQuery.isTotalCount()) {
			// perform count query first, to get total count of records
			String countSelect = "SELECT COUNT(elts) ";
			Object countResult;
			Query countQuery = entityManager.createQuery(countSelect + queryStr);
			countResult = countQuery.getSingleResult();
			if (countResult != null) {
				resultSet.setTotal(((Long) countResult).intValue());
			}
		}
		List<T> result;
		Query query = entityManager.createQuery(select + queryStr);
		if (simpleQuery.getLimit() > 0) {
			query.setFirstResult(simpleQuery.getOffset());
			query.setMaxResults(simpleQuery.getLimit());
		}
		result = query.getResultList();
        resultSet.setResult(result);
		logger.debug("Got: " + result.size() + " results");
		logger.debug("\r");
		return resultSet;
	}
	
	/**
	 * executeSimpleQueryWithPagingAndACLs() execute a simple query with paging restricted by ACLs and return a list of matching objects;
	 * @param simpleQuery
	 * @return
	 */	
	private <T> ResultSet<T> executeSimpleQueryWithPagingAndACL(EntityManager entityManager, SimpleQuery simpleQuery) {
		// Caution !!: assumes the entity name is same as JavaClassName
		
		String entity = simpleQuery.getEntityClass().getSimpleName();		
		String tenantId = simpleQuery.getTenantId();		// assume we have a tenantId as ACL restricted queries are always in context of a tenant
		AccountACL acl = simpleQuery.getAcl().getEntityAcls(entity);
		String aceIdList = getAceIdList(acl.getAcl());
		
		String queryStr;
		String select = "SELECT DISTINCT elts.* FROM " + entity + " elts, ACE ace ";
		String where  = "WHERE (elts.tenantId = '" + tenantId + "') ";
		where += (" AND (ace.tenantId = '" + tenantId + "') ");
		where += (" AND ace.id IN (" + aceIdList + ")");
		where += (" AND elts.path REGEXP ace.pattern ");
		
		if (StringUtils.isNotBlank(simpleQuery.getFilter())) {
			where += ("AND " + "(" + buildFilterClause(simpleQuery.getFilter()) + ") ");
		}

        // because we do native query we need to get rid of "primaryKey."
        where = where.replaceAll("primaryKey.", "");

        ResultSet<T> rs = new ResultSet<>();
		if (simpleQuery.isTotalCount()) {
			// perform count query first, to get total count of records
			String countSelect = "SELECT COUNT(DISTINCT elts.id) FROM " + entity + " elts, ACE ace ";
			queryStr  = countSelect + where;
			logger.debug("Executing Count query: " + queryStr);
			Object countResult;
			Query countQuery = entityManager.createNativeQuery(queryStr);
			countResult = countQuery.getSingleResult();
			if (countResult != null) {
				rs.setTotal(((BigInteger) countResult).intValue());
			}
		}

		// now perform actual query with paging/sorting if specified
		queryStr = select + where;
		if (StringUtils.isNotBlank(simpleQuery.getOrderByField())) {
			queryStr += " ORDER BY elts." + simpleQuery.getOrderByField() +
                    (simpleQuery.isOrderAscending() ? " ASC " : " DESC");
		}
		Query query = entityManager.createNativeQuery(queryStr, simpleQuery.getEntityClass());
		if (simpleQuery.getLimit() > 0) {
			query.setFirstResult(simpleQuery.getOffset());
			query.setMaxResults(simpleQuery.getLimit());
		}
		logger.debug("Executing query: " + queryStr);
		List<T> result = query.getResultList();
		addPermissions(result, acl);
        rs.setResult(result); 
		logger.debug("Got: " + result.size() + " results");
		return rs;
	}

	/**
	 * executeSimpleQuery() execute a simple query WITHOUT paging, with or without ACLs and return a list of matching objects
	 * @param simpleQuery
	 * @return
	 */	
	public <T> Collection<T> executeSimpleQuery(EntityManager entityManager, SimpleQuery simpleQuery) {
		// Caution !!: assumes the entity name is same as JavaClassName

		if (simpleQuery.getAcl() != null) {
			// query with ACLs
			return executeSimpleQueryWithACL(entityManager, simpleQuery);
		}

		String entity = simpleQuery.getEntityClass().getSimpleName();
		String queryStr = "SELECT elts FROM " + entity + " elts ";
		boolean where = false;
		String tenantId ;
		if (!StringUtils.isBlank(simpleQuery.getTenantId())) {
			where = true;
			tenantId = simpleQuery.getTenantId();
			queryStr += ("WHERE (elts.primaryKey.tenantId = '" + tenantId + "') ");
		}

		if (StringUtils.isNotBlank(simpleQuery.getFilter())) {
			queryStr += (where ? "AND " : "WHERE ") + "(" + buildFilterClause(simpleQuery.getFilter()) + ") ";
		}
		
		if (!StringUtils.isBlank(simpleQuery.getOrderByField())) {
			queryStr += " ORDER BY elts." + simpleQuery.getOrderByField() +
                    (simpleQuery.isOrderAscending() ? " ASC " : " DESC");
		}
		logger.debug("Executing query: " + queryStr);
		List<T> result;
		Query query = entityManager.createQuery(queryStr);
		if (simpleQuery.getLimit() > 0) {
			query.setFirstResult(simpleQuery.getOffset());
			query.setMaxResults(simpleQuery.getLimit());
		}
		result = query.getResultList();
		logger.debug("Got: " + result.size() + " results");
		return result;
	}

	/**
	 * executeSimpleQueryWithPagingAndACLs() execute a simple query WITHOUT paging restricted by ACLs and return a list of matching objects;
	 * @param simpleQuery
	 * @return
	 */	
	private <T> Collection<T> executeSimpleQueryWithACL(EntityManager entityManager, SimpleQuery simpleQuery) {
		// Caution !!: assumes the entity name is same as JavaClassName
		
		String entity = simpleQuery.getEntityClass().getSimpleName();		
		String tenantId = simpleQuery.getTenantId();		// assume we have a tenantId as ACL restricted queries are always in context of a tenant
		AccountACL acl = simpleQuery.getAcl().getEntityAcls(entity);
		String aceIdList = getAceIdList(acl.getAcl());
		
		String select = "SELECT DISTINCT elts.* FROM " + entity + " elts, ACE ace ";
		String where  = "WHERE (elts.tenantId = '" + tenantId + "') ";
		where += (" AND (ace.tenantId = '" + tenantId + "') ");
		where += (" AND ace.id IN (" + aceIdList + ")");
		where += (" AND elts.path REGEXP ace.pattern ");
		
		if (StringUtils.isNotBlank(simpleQuery.getFilter())) {
			where += ( "AND " + "(" + buildFilterClause(simpleQuery.getFilter()) + ") ");
		}
		String queryStr = select + where;
		if (StringUtils.isNotBlank(simpleQuery.getOrderByField())) {
			queryStr += (" ORDER BY elts." + simpleQuery.getOrderByField() + (simpleQuery.isOrderAscending() ? " ASC " : " DESC"));
		}

		// now perform query 
		Query query = entityManager.createNativeQuery(queryStr, simpleQuery.getEntityClass());
		logger.debug("Executing query: " + queryStr);
		List<T> result = query.getResultList();		
		addPermissions(result, acl);
		logger.debug("Got: " + result.size() + " results");
		return result;
	}

	/**
	 * executeSimpleQuery() execute a simple query and return a list of matching objects, converted to the specified target class
	 * @param sq
	 * @return
	 * @throws InvocationTargetException 
	 * @throws IllegalArgumentException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 */
	public <E, T> Collection<T> executeSimpleQuery(EntityManager em, SimpleQuery sq, Class<? extends T> tgtClass)
            throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Collection<E> collection = executeSimpleQuery(em, sq);
		DtoMapper<E, T> mapper = new DtoMapper<>();
		return mapper.map(collection, tgtClass);
	}

    public <E, T> Collection<T> executeGetAssociated(EntityManager entityManager, SimpleQuery simpleQuery,
                                                     PrimaryKey primaryKey, Class<E> entityClass,
                                                     String collectionProp)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String inClause = getIdInClause(entityManager, primaryKey, entityClass, collectionProp);
        if (StringUtils.isEmpty(inClause)) {
            return new ArrayList<>();
        } else {
            simpleQuery.addFilter(inClause);
            return executeSimpleQuery(entityManager, simpleQuery);
        }
    }

    public <E, T> Collection<T> executeGetUnassociated(EntityManager entityManager, SimpleQuery simpleQuery,
                                                       PrimaryKey primaryKey, Class<E> entityClass,
                                                       String collectionProp)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String notInClause = getIdNotInClause(entityManager, primaryKey, entityClass, collectionProp);
        simpleQuery.addFilter(notInClause);
        return executeSimpleQuery(entityManager, simpleQuery);
    }

    public <E, T> ResultSet<T> executeGetUnassociatedWithPaging(EntityManager entityManager, SimpleQuery simpleQuery,
                                                                PrimaryKey primaryKey, Class<E> entityClass,
                                                                String collectionProp)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String notInClause = getIdNotInClause(entityManager, primaryKey, entityClass, collectionProp);
        simpleQuery.addFilter(notInClause);
        return executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }

    public <E, T> ResultSet<T> executeGetUnassociatedWithPaging(EntityManager entityManager, SimpleQuery simpleQuery,
                                                                PrimaryKey primaryKey, Class<E> entityClass,
                                                                String collectionProp, String linkField)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String notInClause = getIdNotInClause(entityManager, primaryKey, entityClass, collectionProp, linkField);
        simpleQuery.addFilter(notInClause);
        return executeSimpleQueryWithPaging(entityManager, simpleQuery);
    }

    public <E, T> ResultSet<T> executeGetAssociatedWithPaging(EntityManager entityManager, SimpleQuery simpleQuery,
                                                              PrimaryKey primaryKey, Class<E> entityClass,
                                                              String collectionProp)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String inClause = getIdInClause(entityManager, primaryKey, entityClass, collectionProp);
        if (StringUtils.isEmpty(inClause)) {
            return new ResultSet<>();
        } else {
            simpleQuery.addFilter(inClause);
            return executeSimpleQueryWithPaging(entityManager, simpleQuery);
        }
    }

    public <E, T> ResultSet<T> executeGetAssociatedWithPaging(EntityManager entityManager, SimpleQuery simpleQuery,
                                                              PrimaryKey primaryKey, Class<E> entityClass,
                                                              String collectionProp, String linkField)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String inClause = getIdInClause(entityManager, primaryKey, entityClass, collectionProp, linkField);
        if (StringUtils.isEmpty(inClause)) {
            return new ResultSet<>();
        } else {
            simpleQuery.addFilter(inClause);
            return executeSimpleQueryWithPaging(entityManager, simpleQuery);
        }
    }

    private <E, T> String getIdNotInClause(EntityManager entityManager, PrimaryKey primaryKey, Class<E> entityClass,
                                           String collectionProp)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Collection<T> collection = getAssociatedEntities(entityManager, primaryKey, entityClass, collectionProp);
        String inClause = buildInClause(collection);
        boolean hasPrimaryKey = itemHasPrimaryKey(collection);

        return StringUtils.isEmpty(inClause) ? "" : (hasPrimaryKey ? "primaryKey." : "") + "id not in (" + inClause
                + ")";
    }

    private <E, T> String getIdNotInClause(EntityManager entityManager, PrimaryKey primaryKey, Class<E> entityClass,
                                           String collectionProp, String linkField)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Collection<T> collection = getAssociatedEntities(entityManager, primaryKey, entityClass, collectionProp);
        String inClause = buildInClause(collection, linkField);
        boolean hasPrimaryKey = itemHasPrimaryKey(collection);

        return StringUtils.isEmpty(inClause) ? "" : (hasPrimaryKey ? "primaryKey." : "") + "id not in (" + inClause
                + ")";
    }

    private <E, T> String getIdInClause(EntityManager entityManager, PrimaryKey primaryKey, Class<E> entityClass,
                                        String collectionProp)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Collection<T> collection = getAssociatedEntities(entityManager, primaryKey, entityClass, collectionProp);
        String inClause = buildInClause(collection);
        boolean hasPrimaryKey = itemHasPrimaryKey(collection);

        return StringUtils.isEmpty(inClause) ? "" : (hasPrimaryKey ? "primaryKey." : "") + "id in (" + inClause + ")";
    }

    private <E, T> String getIdInClause(EntityManager entityManager, PrimaryKey primaryKey, Class<E> entityClass,
                                        String collectionProp, String linkField)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        Collection<T> collection = getAssociatedEntities(entityManager, primaryKey, entityClass, collectionProp);
        String inClause = buildInClause(collection, linkField);
        boolean hasPrimaryKey = itemHasPrimaryKey(collection);

        return StringUtils.isEmpty(inClause) ? "" : (hasPrimaryKey ? "primaryKey." : "") + "id in (" + inClause + ")";
    }

    private <T> String buildInClause(Collection<T> collection)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String result = "";
        boolean first = true;
        for (T itemEntity : collection) {
            if (first) {
                first = false;
            } else {
                result += ", ";
            }
            Method getterId = itemEntity.getClass().getMethod("getId");
            if (getterId.getReturnType().isEnum()) {
                result += ((Enum) getterId.invoke(itemEntity)).ordinal();
            } else {
                result += "'" + getterId.invoke(itemEntity) + "'";
            }
        }

        return result;
    }

    private <T> String buildInClause(Collection<T> collection, String linkField)
            throws NoSuchFieldException, IllegalArgumentException, IllegalAccessException, NoSuchMethodException,
            InvocationTargetException {
        String result = "";
        boolean first = true;
        for (T itemEntity : collection) {
            if (first) {
                first = false;
            } else {
                result += ", ";
            }
            String getterLinkEntityName = "get" + linkField.substring(0, 1).toUpperCase() + linkField.substring(1);
            Method getterLinkEntity = itemEntity.getClass().getMethod(getterLinkEntityName);
            Object linkEntity = getterLinkEntity.invoke(itemEntity);
            Method getterId = linkEntity.getClass().getMethod("getId");
            if (getterId.getReturnType().isEnum()) {
                result += ((Enum) getterId.invoke(linkEntity)).ordinal();
            } else {
                result += "'" + getterId.invoke(linkEntity) + "'";
            }
        }

        return result;
    }

    private <T> boolean itemHasPrimaryKey(Collection<T> collection) {
        boolean result = true;
        try {
            T itemEntity = collection.iterator().next();
            itemEntity.getClass().getMethod("getPrimaryKey");
        } catch (Exception e) {
            result = false;
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private <E, T> Collection<T> getAssociatedEntities(EntityManager entityManager, PrimaryKey primaryKey,
                                                       Class<E> entityClass, String collectionProp)
            throws IllegalAccessException, NoSuchFieldException {
        E entity = entityManager.find(entityClass, primaryKey);

        Field field = EmlogisUtils.findFieldByName(entityClass, collectionProp);
        field.setAccessible(true);
        return (Collection<T>) field.get(entity);
    }

	private String getAceIdList(List<AccountACE> acl) {
		String result = "";
		Iterator<AccountACE> it = acl.iterator();
		while (it.hasNext()) {
			AccountACE ace = it.next();
			result += ("'" + ace.getId() + "'");
			if (it.hasNext()) {
				result += ",";
			}			
		}
		return StringUtils.isNotBlank(result) ? result : null;
	}

	private void addPermissions(List<?> list, AccountACL acls) {
		for (Object obj : list) {
			ACLProtected entity = (ACLProtected) obj;
			ACLUtil.setPermissions(entity, acls);
		}	
	}

    public static void tryAddNotDeletedFilter(SimpleQuery simpleQuery) {
        tryAddNotDeletedFilter(simpleQuery, QueryPattern.NOT_DELETED.val());
    }

    public static void tryAddNotDeletedFilter(SimpleQuery simpleQuery, String filter) {
        if(simpleQuery == null){
            return;
        }
        if (StringUtils.isEmpty(simpleQuery.getFilter()) || !simpleQuery.getFilter().contains("isDeleted")) {
            simpleQuery.addFilter(filter);
        }
    }
}
