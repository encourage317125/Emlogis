package com.emlogis.common.services.workflow.process.update.asynch.callables;

import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.proxies.AvailabilityActionProxy;
import com.emlogis.model.workflow.entities.WorkflowRequest;

import java.util.concurrent.Callable;

/**
 * Created by user on 07.07.15.
 */
public class AvailabilityActionThread implements Callable<ResultPair> {

    private AvailabilityActionProxy proxy;
    private WorkflowRequest request;

    public AvailabilityActionThread(
            AvailabilityActionProxy proxy,
            WorkflowRequest request
    ) {
        this.proxy = proxy;
        this.request = request;
    }

    @Override
    public ResultPair call() throws Exception {
        try {
            return proxy.execute(request);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }
}
