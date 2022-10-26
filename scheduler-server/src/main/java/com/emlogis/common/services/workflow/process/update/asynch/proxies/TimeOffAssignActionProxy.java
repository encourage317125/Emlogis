package com.emlogis.common.services.workflow.process.update.asynch.proxies;

import com.emlogis.common.facade.schedule.ScheduleFacade;
import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.common.WorkflowActionProxy;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.dto.CDAvailabilityTimeFrameDto;
import com.emlogis.model.schedule.QualificationRequestSummary;

import javax.ejb.*;

/**
 * Created by user on 26.08.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TimeOffAssignActionProxy extends WorkflowActionProxy {

    @EJB
    private ScheduleFacade scheduleFacade;

    public ResultPair execute(
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
        try {
            QualificationRequestSummary result = null;
            try {
                result = scheduleFacade.manualShiftWIP(schedulePk, shiftPk, wipEmployeePk, force,
                        requestPk.getId(), managerPk.getId(), reason);
                if (!result.isSuccess()) {
                    return new ResultPair(false, result.getMessage());
                }
            } catch (Exception ex) {
                return new ResultPair(false, ex.getMessage());
            }
            try {
                CDAvailabilityTimeFrameDto cdAvailabilityTimeFrame = timeframe(employee, absenceType, reason, requestDate);
                if (cdAvailabilityTimeFrame == null) {
                    return new ResultPair(false, "Can not create Availability time frame!");
                }
            } catch (Exception ex) {
                return new ResultPair(false, ex.getMessage());
            }
            return new ResultPair(result.isSuccess(), result.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultPair(false, e.getMessage());
        }
    }
}
