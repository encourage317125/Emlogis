package com.emlogis.common.services.workflow.process.update.asynch.proxies;

import com.emlogis.common.facade.schedule.ScheduleFacade;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.workflow.TranslationParam;
import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.common.WorkflowActionProxy;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.schedule.QualificationRequestSummary;

import javax.ejb.*;

import static javax.ejb.TransactionAttributeType.NOT_SUPPORTED;

/**
 * Created by user on 11.06.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class OpenShiftActionProxy extends WorkflowActionProxy {

    @EJB
    private ScheduleFacade scheduleFacade;

    @EJB
    private EmployeeService employeeService;

    @TransactionAttribute(NOT_SUPPORTED)
    public ResultPair execute(
            Boolean isAutoApproval,
            PrimaryKey schedulePk,
            PrimaryKey shiftPk,
            PrimaryKey employeePk,
            Boolean force,
            String employeeName,
            String reason,
            PrimaryKey requestPk,
            PrimaryKey managerPk
    ) {
        try {
            PrimaryKey requesterPk;
            if (isAutoApproval) {
                requesterPk = employeeService.getEmployee(managerPk).getUserAccount().getPrimaryKey();
            } else {
                requesterPk = managerPk;
            }
            QualificationRequestSummary summary = scheduleFacade.manualShiftOpenAssign(schedulePk, shiftPk,
                    employeePk, force, requestPk.getId(), requesterPk.getId(), reason, null);
            if (summary.getFullyQualified()) {
                return new ResultPair(true, "success");
            } else {
                TranslationParam[] params = {
                        new TranslationParam("scheduleId", schedulePk.getId()),
                        new TranslationParam("shiftId", shiftPk.getId()),
                        new TranslationParam("employeeName", employeeName),
                        new TranslationParam("employeeId", employeePk.getId()),
                };
                return new ResultPair(false, translate("request.action.open.shift.error", params));
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new ResultPair(false, e.getMessage());
        }
    }
}
