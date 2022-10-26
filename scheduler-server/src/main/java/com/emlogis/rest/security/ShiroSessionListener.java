package com.emlogis.rest.security;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShiroSessionListener implements SessionListener {
	
	private final Logger logger = LoggerFactory.getLogger(ShiroSessionListener.class);

    @Override
    public void onStart(Session session) {
        logger.debug(String.format("On start shiro session [sessionId=%s]", session.getId()));
    }

    @Override
    public void onStop(Session session) {
        logger.debug(String.format("On stop shiro session [sessionId=%s]", session.getId()));
    }

    @Override
    public void onExpiration(Session session) {
        logger.debug(String.format("On expiration shiro session [sessionId=%s]", session.getId()));
    }

}
