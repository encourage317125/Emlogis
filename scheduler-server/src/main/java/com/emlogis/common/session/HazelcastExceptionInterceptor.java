package com.emlogis.common.session;

import com.emlogis.common.services.hazelcast.HazelcastClientService;
import com.emlogis.server.services.ASHazelcastService;
import com.emlogis.server.services.SchedulingResponseServiceBean;
import com.hazelcast.core.HazelcastInstanceNotActiveException;

import javax.ejb.EJB;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

@HazelcastExceptionHandling
@Interceptor
public class HazelcastExceptionInterceptor {

    @EJB
    private ASHazelcastService hazelcastService;

    @AroundInvoke
    public Object around(InvocationContext context) throws Exception {
        try {
            return context.proceed();
        } catch (HazelcastInstanceNotActiveException he) {
            hazelcastService.init();

            Object target = context.getTarget();
            if (target instanceof SchedulingResponseServiceBean) {
                ((SchedulingResponseServiceBean) target).init();
            } else if (target instanceof HazelcastClientService) {
                ((HazelcastClientService) target).init();
            }

            return context.proceed();
        }
    }

}
