package com.emlogis.common.services.workflow.process.update.asynch.callables;

import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.proxies.WipActionProxy;
import com.emlogis.model.PrimaryKey;

import java.util.concurrent.Callable;

/**
 * Created by user on 11.06.15.
 */
public class WipActionThread implements Callable<ResultPair> {

    private WipActionProxy proxy;
    private PrimaryKey schedulePk;
    private PrimaryKey shiftPk;
    private PrimaryKey wipEmployeePk;
    private Boolean force;
    private String reason;
    private PrimaryKey requestPk;
    private PrimaryKey managerPk;

    public WipActionThread(
            WipActionProxy proxy,
            PrimaryKey schedulePk,
            PrimaryKey shiftPk,
            PrimaryKey wipEmployeePk,
            Boolean force,
            String reason,
            PrimaryKey requestPk,
            PrimaryKey managerPk
    ) {
        this.proxy = proxy;
        this.schedulePk = schedulePk;
        this.shiftPk = shiftPk;
        this.wipEmployeePk = wipEmployeePk;
        this.force = force;
        this.reason = reason;
        this.requestPk = requestPk;
        this.managerPk = managerPk;
    }

    @Override
    public ResultPair call() throws Exception {
        try {
            return proxy.execute(schedulePk, shiftPk, wipEmployeePk, force, reason, requestPk, managerPk);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }
}
