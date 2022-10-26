package com.emlogis.common.services.workflow.process.update.asynch.callables;

import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.proxies.DropShiftActionProxy;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.Employee;

import java.util.concurrent.Callable;

/**
 * Created by user on 11.06.15.
 */
public class DropShiftActionThread implements Callable<ResultPair> {

    private DropShiftActionProxy proxy;
    private PrimaryKey schedulePk;
    private PrimaryKey shiftPk;
    private Employee employee;
    private AbsenceType absenceType;
    private Long requestDate;
    private String reason;
    private PrimaryKey requestPk;
    private PrimaryKey managerPk;

    public DropShiftActionThread(
            DropShiftActionProxy proxy,
            PrimaryKey schedulePk,
            PrimaryKey shiftPk,
            Employee employee,
            AbsenceType absenceType,
            Long requestDate,
            String reason,
            PrimaryKey requestPk,
            PrimaryKey managerPk
    ) {
        this.proxy = proxy;
        this.schedulePk = schedulePk;
        this.shiftPk = shiftPk;
        this.employee = employee;
        this.absenceType = absenceType;
        this.requestDate = requestDate;
        this.reason = reason;
        this.requestPk = requestPk;
        this.managerPk = managerPk;
    }

    @Override
    public ResultPair call() throws Exception {
        try {
            return proxy.execute(schedulePk, shiftPk, employee, absenceType, requestDate, reason, requestPk, managerPk);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }
}
