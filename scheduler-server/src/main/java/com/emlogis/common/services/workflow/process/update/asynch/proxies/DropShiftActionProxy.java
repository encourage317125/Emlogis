package com.emlogis.common.services.workflow.process.update.asynch.proxies;

import com.emlogis.common.facade.schedule.ScheduleFacade;
import com.emlogis.common.services.employee.CDAvailabilityTimeFrameService;
import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.common.WorkflowActionProxy;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.dto.CDAvailabilityTimeFrameDto;

import javax.ejb.*;

/**
 * Created by user on 11.06.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class DropShiftActionProxy extends WorkflowActionProxy {

    @EJB
    private ScheduleFacade scheduleFacade;

    @EJB
    private CDAvailabilityTimeFrameService cdAvailabilityTimeFrameService;

    public ResultPair execute(
            PrimaryKey schedulePk,
            PrimaryKey shiftPk,
            Employee employee,
            AbsenceType absenceType,
            Long requestDate,
            String reason,
            PrimaryKey requestPk,
            PrimaryKey managerPk
    ) {
        try {
            scheduleFacade.manualShiftDrop(schedulePk, shiftPk, requestPk.getId(), managerPk.getId(), reason);
            CDAvailabilityTimeFrameDto cdAvailabilityTimeFrame = timeframe(employee, absenceType, reason, requestDate);
            return new ResultPair(true, "success");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultPair(false, e.getMessage());
        }
    }
}
