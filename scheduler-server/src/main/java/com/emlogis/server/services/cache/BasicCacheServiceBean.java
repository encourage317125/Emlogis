package com.emlogis.server.services.cache;

import com.emlogis.model.BaseEntity;
import com.emlogis.shared.services.hazelcastservice.HazelcastService;
import com.hazelcast.core.IMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Startup
@Singleton
@DependsOn("HazelcastServiceBean")
@AccessTimeout(10000)	// 10 secs delay: allow more than the default 5sec for client EJBs to connect to this Service 
						// as connection to Hazelcast on startup can take few secs and potentially more than 5secs
public class BasicCacheServiceBean implements BasicCacheService {

	private final Logger logger = LoggerFactory.getLogger(BasicCacheServiceBean.class);
	
	private  Map<String, IMap<String, Object>> cacheMap = new HashMap<>();	// hazelcast maps keyed by cache name
	
	@Inject
	private HazelcastService hzService;	

    @PostConstruct
    void init() {}

    @Override
	public Object getEntry(String cacheName, String tenantId) {
    	return getCacheEntry(cacheName, tenantId);
    }
   
    @Override
	public Object getEntry(String cacheName, String tenantId, String key) {
    	return getCacheEntry(cacheName, getKeyPerTenant(tenantId, key));
    }

    @Override
	public Object putEntry(String cacheName, String tenantId, String value) {
    	return putCacheEntry(cacheName, tenantId, value);
    }
    
    @Override
	public Object putEntry(String cacheName, String tenantId, String key, Serializable entity) {
    	return putCacheEntry(cacheName, getKeyPerTenant(tenantId, key), entity);
    }
    
    @Override
	public Object clearEntry(String cacheName, String tenantId) {
    	return clearCacheEntry(cacheName, tenantId);
    }

    @Override
	public Object clearEntry(String cacheName, String tenantId, String key) {
    	return clearCacheEntry(cacheName, getKeyPerTenant(tenantId, key));
    }

    @Override
	public void clearCache(String cacheName) {
        IMap<String, Object> map = getCache(cacheName);
        map.clear();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Collection<? extends BaseEntity> getEntries(String cacheName, String tenantId) {
        return (Collection<? extends BaseEntity>) getCacheEntry(cacheName, tenantId);
    }

    @Override
    public void putEntry(String cacheName, String tenantId, Collection<? extends BaseEntity> entities) {
        putCacheEntry(cacheName, tenantId, entities);
    }

    @Override
    public void putEntry(String cacheName, String tenantId, Serializable entity) {
        putCacheEntry(cacheName, tenantId, entity);
    }

    private Object clearCacheEntry(String cacheName, String key) {
        IMap<String, Object> map = getCache(cacheName);
        return map.remove(key);
    }

    private Object putCacheEntry(String cacheName, String key, Object value) {
        IMap<String, Object> map = getCache(cacheName);
        return map.put(key, value);
    }

    private Object getCacheEntry(String cacheName, String key) {
        IMap<String, Object> map = getCache(cacheName);
        return map.get(key);
    }

    private IMap<String, Object> getCache(String cacheName) {
    	IMap<String, Object> map = cacheMap.get(cacheName);
    	if (map == null) {
    		map = hzService.getInstance().getMap(cacheName);
    		cacheMap.put(cacheName, map);
    	}
    	return map;	
    }

    private String getKeyPerTenant(String tenantId, String key) {
		return tenantId + ":" + key;
	}
    
}



