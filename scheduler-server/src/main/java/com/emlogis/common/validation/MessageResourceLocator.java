package com.emlogis.common.validation;

import com.emlogis.rest.security.SessionService;

public class MessageResourceLocator {

    private SessionService sessionService;

    public SessionService getSessionService() {
        return sessionService;
    }

    public void setSessionService(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    public String getMessage(String code, Object... params) {
        return sessionService.getMessage(code, params);
    }
}
