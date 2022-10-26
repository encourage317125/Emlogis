package com.emlogis.server.services.cache;

import com.emlogis.model.BaseEntity;

import java.io.Serializable;
import java.util.Collection;

public interface BasicCacheService {

	/**
	 * Creates a cache with a maximum number of entries and LRU eviction policies
	 * creating a cache is optional. if createCache is not invoked, then a cache with no max size will be used.
	 * Note that in case a cache must be created with a size, then createCache must be the very first call invoked.
	 *  
	 * @param cacheName
	 * @param maxEntries
	 * @return
	 *
	 * NOTE THIS METHOD  IS ON HOLD CURRENTLY because Hz guideline calls for an external configuration file vs a programmatic 
	 * configuration, so as to make sure the config is the same on all Nodes (Which is a requirement)
	 * 
	*/
	// boolean createCache(String cacheName, int maxEntries);

	void clearCache(String cacheName);

	Object getEntry(String cacheName, String tenantId, String key);
	Object getEntry(String cacheName, String tenantId);
	Collection<? extends BaseEntity> getEntries(String cacheName, String tenantId);

	Object putEntry(String cacheName, String tenantId, String key, Serializable entity);
	Object putEntry(String cacheName, String tenantId, String value);
	void putEntry(String cacheName, String tenantId, Collection<? extends BaseEntity> entities);
	void putEntry(String cacheName, String tenantId, Serializable entity);

	Object clearEntry(String cacheName, String tenantId);
	Object clearEntry(String cacheName, String tenantId, String key);

}
