package com.emlogis.common.services.workflow.process.update.asynch.callables;

import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.proxies.TimeOffNoActionProxy;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.Employee;

import java.util.concurrent.Callable;

/**
 * Created by user on 14.09.15.
 */
public class TimeOffNoActionThread implements Callable<ResultPair> {

    private Employee employee;
    private AbsenceType absenceType;
    private Long requestDate;
    private String reason;
    private TimeOffNoActionProxy proxy;

    public TimeOffNoActionThread(
            Employee employee,
            AbsenceType absenceType,
            Long requestDate,
            String reason,
            TimeOffNoActionProxy proxy
    ) {
        this.employee = employee;
        this.absenceType = absenceType;
        this.requestDate = requestDate;
        this.reason = reason;
        this.proxy = proxy;
    }

    @Override
    public ResultPair call() throws Exception {
        try {
            return proxy.execute(employee, absenceType, requestDate, reason);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }
}
