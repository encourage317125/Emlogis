package com.emlogis.common.session;

import com.emlogis.common.EmlogisUtils;
import com.emlogis.common.ResourcesBundle;
import com.emlogis.rest.security.BackendSessionInfo;
import com.emlogis.rest.security.SessionService;
import com.emlogis.scheduler.engine.communication.HzConstants;
import com.emlogis.server.services.eventservice.ASEventService;
import com.emlogis.shared.services.eventservice.EventKeyBuilder;
import com.emlogis.shared.services.eventservice.EventScope;
import com.emlogis.shared.services.eventservice.EventService;
import com.emlogis.shared.services.hazelcastservice.HazelcastInstanceBuilder;
import com.gs.collections.impl.map.mutable.MapAdapter;
import com.hazelcast.core.*;
import com.hazelcast.map.listener.MapListener;
import org.apache.log4j.Logger;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.apache.shiro.util.CollectionUtils;
import reactor.event.Event;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class HazelcastSessionDAO extends AbstractSessionDAO {

	private final static Logger logger = Logger.getLogger(HazelcastSessionDAO.class);

	private final static long FIVE_MINUTES = 5L * 60 * 1000;

    private IMap<Object, Object> sessionNotificationMap;
    private IMap<Object, byte[]> sessionMap;

    private SessionListener sessionListener;

    private ASEventService eventService;

    private ResourcesBundle resourcesBundle;

    interface HazelcastComponentAccess {
        Object perform();
    }

    public HazelcastSessionDAO() {
        init();
    }

    public SessionListener getSessionListener() {
        return sessionListener;
    }

    public void setSessionListener(SessionListener sessionListener) {
        this.sessionListener = sessionListener;
    }

    public ASEventService getEventService() {
        return eventService;
    }

    public void setEventService(ASEventService eventService) {
        this.eventService = eventService;
    }

    public ResourcesBundle getResourcesBundle() {
        return resourcesBundle;
    }

    public void setResourcesBundle(ResourcesBundle resourcesBundle) {
        this.resourcesBundle = resourcesBundle;
    }

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        storeSession(sessionId, session);
        return sessionId;
    }

    protected Session storeSession(final Serializable id, final Session session) {
        if (id == null) {
            throw new NullPointerException("id argument cannot be null.");
        }
        final long notificationTtl = session.getTimeout() - FIVE_MINUTES;
        if (notificationTtl > 0) {
            performHazelcastAccess(new HazelcastComponentAccess() {
                @Override
                public Object perform() {
                    sessionNotificationMap.put(id, notificationTtl, notificationTtl, TimeUnit.MILLISECONDS);
                    return null;
                }
            });
        }
        
        final byte[] sessionBytes = EmlogisUtils.serializeObject(session);

        performHazelcastAccess(new HazelcastComponentAccess() {
            @Override
            public Object perform() {
                sessionMap.put(id, sessionBytes, session.getTimeout(), TimeUnit.MILLISECONDS);
                return null;
            }
        });

        return session;
    }

    @Override
    protected Session doReadSession(final Serializable sessionId) {
        return (Session) performHazelcastAccess(new HazelcastComponentAccess() {
            @Override
            public Object perform() {
                byte[] sessionBytes = sessionMap.get(sessionId);
                return EmlogisUtils.deserializeObject(sessionBytes);
            }
        });
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        storeSession(session.getId(), session);
    }

    @Override
    public void delete(Session session) {
        if (session == null) {
            throw new NullPointerException("session argument cannot be null.");
        }
        final Serializable id = session.getId();
        if (id != null) {
            performHazelcastAccess(new HazelcastComponentAccess() {
                @Override
                public Object perform() {
                    sessionMap.remove(id);
                    return null;
                }
            });
        }
    }

    public Collection<Session> getActiveSessions() {
        Collection<Session> values = new HashSet<>();
        for (byte[] sessionBytes : sessionMap.values()) {
            Session session = (Session) EmlogisUtils.deserializeObject(sessionBytes);
            values.add(session);
        }

        if (CollectionUtils.isEmpty(values)) {
            return Collections.emptySet();
        } else {
            return Collections.unmodifiableCollection(values);
        }
    }

    public Session getSession(Serializable sessionId) {
        return doReadSession(sessionId);
    }

    private void sendEvent(Session session, String messageCode) {
        String tenantId = null;
        String message = null;
        BackendSessionInfo sessionInfo = (BackendSessionInfo) EmlogisUtils.deserializeObject(
                (byte[]) session.getAttribute(SessionService.SESSION_INFO_PROP));
        if (sessionInfo != null) {
            tenantId = sessionInfo.getTenantId();
            message = resourcesBundle.getMessage(sessionInfo.getLanguage(), messageCode);
        }

        Object key = new EventKeyBuilder().setTopic(EventService.TOPIC_SYSTEM)
                .setTenantId(tenantId).setEventType("Notification")
                .setEntityClass("SSEClient").build();

        try {
            eventService.sendEvent(EventScope.Local, key, Event.wrap(message), "SSEServlet");
        } catch (InterruptedException e) {
            logger.error("Error of sending session event", e);
        }
    }

    private void init() {
        HazelcastInstance hazelcastInstance = HazelcastInstanceBuilder.newHazelcastClient();
        sessionMap = hazelcastInstance.getMap(HzConstants.SESSION_MAP);
        sessionNotificationMap = hazelcastInstance.getMap(HzConstants.SESSION_NOTIFICATION_MAP);

        sessionMap.addEntryListener(new EntryAdapter<Object, byte[]>() {
            @Override
            public void entryEvicted(EntryEvent<Object, byte[]> event) {
                logger.debug("Evicted session " + event.getKey());

                Session session = (Session) EmlogisUtils.deserializeObject(event.getValue());

                sendEvent(session, "event.session.expired");

                if (sessionListener != null) {
                    sessionListener.onExpiration(session);
                }
            }
        }, true);

        final BlockingQueue<Object> notificationQueue = new LinkedBlockingQueue<>();

        sessionNotificationMap.addEntryListener(new EntryAdapter<Object, Object>() {
            @Override
            public void entryEvicted(EntryEvent<Object, Object> event) {
                notificationQueue.offer(event.getKey());
            }
        }, true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Object id = notificationQueue.take();
                        Session session = (Session) EmlogisUtils.deserializeObject(sessionMap.get(id));
                        if (session != null) {
                            sendEvent(session, "event.session.5min.to.expiration");
                        }
                    } catch (InterruptedException e) {
                        logger.error("hazelcast error",e);
                    }
                }
            }
        }).start();
    }

    private Object performHazelcastAccess(HazelcastComponentAccess access) {
        try {
            return access.perform();
        } catch (HazelcastException e) {
            init();
            return access.perform();
        }
    }
}
