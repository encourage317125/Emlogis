package com.emlogis.common.services.workflow.process.update.asynch.callables;

import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.proxies.ShiftSwapActionProxy;
import com.emlogis.model.PrimaryKey;

import java.util.concurrent.Callable;

/**
 * Created by user on 11.06.15.
 */
public class ShiftSwapActionThread implements Callable<ResultPair> {

    private ShiftSwapActionProxy proxy;
    private PrimaryKey schedulePk;
    private PrimaryKey shiftAPk;
    private PrimaryKey shiftBPk;
    private Boolean force;
    private String reason;
    private PrimaryKey requestPk;
    private PrimaryKey managerPk;

    public ShiftSwapActionThread(
            ShiftSwapActionProxy proxy,
            PrimaryKey schedulePk,
            PrimaryKey shiftAPk,
            PrimaryKey shiftBPk,
            Boolean force,
            String reason,
            PrimaryKey requestPk,
            PrimaryKey managerPk
    ) {
        this.proxy = proxy;
        this.schedulePk = schedulePk;
        this.shiftAPk = shiftAPk;
        this.shiftBPk = shiftBPk;
        this.force = force;
        this.reason = reason;
        this.requestPk = requestPk;
        this.managerPk = managerPk;
    }

    @Override
    public ResultPair call() throws Exception {
        try {
            return proxy.execute(schedulePk, shiftAPk, shiftBPk, force, reason, requestPk, managerPk);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }
}
