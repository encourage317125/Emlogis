package com.emlogis.shared.services.eventservice;

import org.apache.commons.lang3.StringUtils;

import com.emlogis.scheduler.engine.communication.request.RequestType;

/**
 * @author EmLogis
 * 
 * Event key format: <topic><tenantId><accountId><entityClass><eventType>[<entityId>]
 *
 */
public class EventKeyBuilder {
	
	private String topic;
	
	private String tenantId;
	
    private String	accountId;
    
	private String 	entityClass;	
	
	private String 	eventType;				

	private String 	entityId;				
	
	
	public Object build(){
		StringBuffer sb  =  new StringBuffer();
		sb.append("<" + (StringUtils.isBlank(topic) ? "" : topic) + ">");
		sb.append("<" + (StringUtils.isBlank(tenantId) ? "" : tenantId) + ">");
		sb.append("<" + (StringUtils.isBlank(accountId) ? "" : accountId) + ">");
		sb.append("<" + (StringUtils.isBlank(entityClass) ? "" : entityClass) + ">");
		sb.append("<" + (StringUtils.isBlank(eventType) ? "" : eventType) + ">");
		sb.append("<" + (StringUtils.isBlank(entityId) ? "" : entityId) + ">");
		return sb.toString();
	}

	public EventKeyBuilder setTopic(String topic) {
		this.topic = topic;
		return this;
	}

	public EventKeyBuilder setTenantId(String tenantId) {
		this.tenantId = tenantId;
		return this;
	}

	public EventKeyBuilder setAccountId(String accountId) {
		this.accountId = accountId;
		return this;
	}

	// prefered method for specifying class name (type check by compiler)
	public EventKeyBuilder setEntityClass(Class entityClass) {
		this.entityClass = (entityClass != null ? entityClass.getSimpleName() : null);
		return this;
	}
	
	// alternate method for specifying class name (for components that do not have access to class, like Schedule class in Engine)
	public EventKeyBuilder setEntityClass(String entityClass) {
		this.entityClass = entityClass;
		return this;
	}

	public EventKeyBuilder setEventType(String eventType) {
		this.eventType = eventType;
		return this;
	}

	public EventKeyBuilder setEntityId(String entityId) {
		this.entityId = entityId;
		return this;
	}

}

