package com.emlogis.common.services.workflow.process.update.asynch.callables;

import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.proxies.PostOpenShiftsActionProxy;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.schedule.PostMode;
import com.emlogis.model.schedule.Schedule;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by user on 11.06.15.
 */
public class PostOpenShiftsActionThread implements Callable<ResultPair> {

    private PostOpenShiftsActionProxy proxy;
    private Schedule schedule;
    private Map<String, Collection<String>> openShiftEmpIdsMap;
    private Long deadline;
    private String comments;
    private Employee employee;
    private AbsenceType absenceType;
    private Long requestDate;
    private String reason;

    public PostOpenShiftsActionThread(
            PostOpenShiftsActionProxy proxy,
            Schedule schedule,
            Map<String, Collection<String>> openShiftEmpIdsMap,
            Long deadline,
            String comments,
            Employee employee,
            AbsenceType absenceType,
            Long requestDate,
            String reason) {
        this.proxy = proxy;
        this.schedule = schedule;
        this.openShiftEmpIdsMap = openShiftEmpIdsMap;
        this.deadline = deadline;
        this.comments = comments;
        this.employee = employee;
        this.absenceType = absenceType;
        this.requestDate = requestDate;
        this.reason = reason;
    }

    @Override
    public ResultPair call() throws Exception {
        try {
            return proxy.execute(schedule, PostMode.Cumulative, openShiftEmpIdsMap, null, deadline, comments, null,
                    employee, absenceType, requestDate, reason);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        }
    }
}
