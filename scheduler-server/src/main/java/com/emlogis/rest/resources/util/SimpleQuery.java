package com.emlogis.rest.resources.util;

import com.emlogis.common.security.AccountACL;
import org.apache.commons.lang3.StringUtils;


public class SimpleQuery {

    public static final String FILTER_DELIMITER = ";";

	private	String		tenantId;			// null = no restriction on tenant
	private	AccountACL 	acl;				// acl to constrain the query. if null, ACLs are not taken into account
	private Class 		entityClass;		// mandatory field
	private String		select;				// null = no select
	private String		filter;				// null = no filter
	private	int			offset;		 		// used only when limit > 0
	private	int			limit = 0;			// -1 = no paging
	private	String		orderByField;		// null = no orderby
	private	boolean		orderAscending = true; // used only when orderby not null 
	private	boolean		totalCount = false;	// if true, result set must contain total count of matching records
	
	public SimpleQuery() {}
	
	public SimpleQuery(String tenantId) {
		this.tenantId = tenantId;
	}
	
	public SimpleQuery(String tenantId, AccountACL acl) {
		this.tenantId = tenantId;
		this.acl = acl;
	}

	public SimpleQuery(String tenantId, Class entityClass) {
		super();
		this.tenantId = tenantId;
		this.entityClass = entityClass;
	}

	public String getTenantId() {
		return tenantId;
	}

	public SimpleQuery setTenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	public AccountACL getAcl() {
		return acl;
	}

	public void setAcl(AccountACL acl) {
		this.acl = acl;
	}

	public Class getEntityClass() {
		return entityClass;
	}

	public SimpleQuery setEntityClass(Class entityClass) {
		this.entityClass = entityClass;
		return this;
	}

	public String getSelect() {
		return select;
	}

	public SimpleQuery setSelect(String select) {
		this.select = select;
		return this;
	}

	public String getFilter() {
		return filter;
	}

	public SimpleQuery setFilter(String filter) {
		this.filter = filter;
		return this;
	}

	public SimpleQuery addFilter(String filter) {
		this.filter = StringUtils.isEmpty(this.filter) ? filter : this.filter + FILTER_DELIMITER + filter;
		return this;
	}

	public int getOffset() {
		return offset;
	}

	public SimpleQuery setOffset(int offset) {
		this.offset = offset;
		return this;
	}

	public int getLimit() {
		return limit;
	}

	public SimpleQuery setLimit(int limit) {
		this.limit = limit;
		return this;
	}

	public String getOrderByField() {
		return orderByField;
	}

	public SimpleQuery setOrderByField(String orderByField) {
		this.orderByField = orderByField;
		return this;
	}

	public boolean isOrderAscending() {
		return orderAscending;
	}

	public SimpleQuery setOrderAscending(boolean orderAscending) {
		this.orderAscending = orderAscending;
		return this;
	}

	public boolean isTotalCount() {
		return totalCount;
	}

	public SimpleQuery setTotalCount(boolean totalCount) {
		this.totalCount = totalCount;
		return this;
	}
		
}