package com.emlogis.common.services.workflow.process.update.asynch.callables;

import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.proxies.TimeOffAssignActionProxy;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.Employee;

import java.util.concurrent.Callable;

/**
 * Created by user on 26.08.15.
 */
public class TimeOffAssignActionThread implements Callable<ResultPair> {

    private TimeOffAssignActionProxy proxy;
    private PrimaryKey schedulePk;
    private PrimaryKey shiftPk;
    private PrimaryKey wipEmployeePk;
    private Boolean force;
    private Employee employee;
    private AbsenceType absenceType;
    private Long requestDate;
    private String reason;
    private PrimaryKey requestPk;
    private PrimaryKey managerPk;

    public TimeOffAssignActionThread(
            TimeOffAssignActionProxy proxy,
            PrimaryKey schedulePk,
            PrimaryKey shiftPk,
            PrimaryKey wipEmployeePk,
            Boolean force,
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
        this.wipEmployeePk = wipEmployeePk;
        this.force = force;
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
            return proxy.execute(schedulePk, shiftPk, wipEmployeePk, force, employee, absenceType, requestDate, reason,
                    requestPk, managerPk);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }
}
