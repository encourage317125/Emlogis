package com.emlogis.common.services.workflow.process.update.asynch.callables;

import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.proxies.OpenShiftActionProxy;
import com.emlogis.model.PrimaryKey;

import java.util.concurrent.Callable;

/**
 * Created by user on 11.06.15.
 */
public class OpenShiftActionThread implements Callable<ResultPair> {

    private OpenShiftActionProxy proxy;
    private PrimaryKey schedulePk;
    private PrimaryKey shiftPk;
    private PrimaryKey employeePk;
    private Boolean force;
    private String employeeName;
    private String reason;
    private PrimaryKey requestPk;
    private PrimaryKey managerPk;
    private Boolean isAutoApproval;

    public OpenShiftActionThread(
            Boolean isAutoApproval,
            OpenShiftActionProxy proxy,
            PrimaryKey schedulePk,
            PrimaryKey shiftPk,
            PrimaryKey employeePk,
            Boolean force,
            String employeeName,
            String reason,
            PrimaryKey requestPk,
            PrimaryKey managerPk
    ) {
        this.isAutoApproval = isAutoApproval;
        this.proxy = proxy;
        this.schedulePk = schedulePk;
        this.shiftPk = shiftPk;
        this.employeePk = employeePk;
        this.force = force;
        this.employeeName = employeeName;
        this.reason = reason;
        this.requestPk = requestPk;
        this.managerPk = managerPk;
    }

    @Override
    public ResultPair call() throws Exception {
        try {
            ResultPair rp = proxy.execute(isAutoApproval, schedulePk, shiftPk, employeePk, force, employeeName, reason, requestPk, managerPk);
            return rp;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }
}
