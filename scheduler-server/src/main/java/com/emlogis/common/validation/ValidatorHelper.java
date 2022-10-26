package com.emlogis.common.validation;

import javax.annotation.Resource;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;

@Stateless
@LocalBean
public class ValidatorHelper {

    @Resource
    private SessionContext context;

    public SessionContext getContext() {
        return context;
    }

}
