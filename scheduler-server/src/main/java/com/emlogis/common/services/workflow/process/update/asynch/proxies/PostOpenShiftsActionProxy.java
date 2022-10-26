package com.emlogis.common.services.workflow.process.update.asynch.proxies;

import com.emlogis.common.services.schedule.PostedOpenShiftService;
import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.common.WorkflowActionProxy;
import com.emlogis.engine.domain.contract.ConstraintOverrideType;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.dto.CDAvailabilityTimeFrameDto;
import com.emlogis.model.schedule.PostMode;
import com.emlogis.model.schedule.Schedule;

import javax.ejb.*;
import java.util.Collection;
import java.util.Map;

/**
 * Created by user on 11.06.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class PostOpenShiftsActionProxy extends WorkflowActionProxy {

    @EJB
    private PostedOpenShiftService postedOpenShiftService;

    public ResultPair execute(
            Schedule schedule,
            PostMode postMode,
            Map<String, Collection<String>> openShiftEmpIdsMap,
            Map<ConstraintOverrideType, Boolean> overrideOptions,
            Long deadline,
            String comments,
            String terms,
            Employee employee,
            AbsenceType absenceType,
            Long requestDate,
            String reason
    ) {
        try {
            postedOpenShiftService.postOpenShifts(schedule, postMode, openShiftEmpIdsMap,
                    overrideOptions, deadline, comments, terms);
            CDAvailabilityTimeFrameDto cdAvailabilityTimeFrame = timeframe(employee, absenceType, reason, requestDate);
            return new ResultPair(true, "success");
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultPair(false, e.getMessage());
        }
    }
}
