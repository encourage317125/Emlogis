package com.emlogis.server.services;

import com.emlogis.common.ResourcesBundle;
import com.emlogis.common.session.HazelcastSessionDAO;
import com.emlogis.server.services.eventservice.ASEventService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.mgt.DefaultSessionManager;
import org.apache.shiro.util.Factory;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;

@Startup
@Singleton
@DependsOn("AOMServiceBean")
public class ShiroInitServiceBean implements ShiroInitService {

    @Inject
    private ASEventService eventService;

    @EJB
    private ResourcesBundle resourcesBundle;

    @PostConstruct
    void init() {
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        SecurityUtils.setSecurityManager(factory.getInstance());

        DefaultSecurityManager securityManager = (DefaultSecurityManager) SecurityUtils.getSecurityManager();
        DefaultSessionManager sessionManager = (DefaultSessionManager) securityManager.getSessionManager();
        HazelcastSessionDAO sessionDAO = (HazelcastSessionDAO) sessionManager.getSessionDAO();

        sessionDAO.setEventService(eventService);
        sessionDAO.setResourcesBundle(resourcesBundle);
    }

}
