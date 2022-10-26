package com.emlogis.common.services.workflow.process.update.asynch.proxies;

import com.emlogis.common.exceptions.ValidationException;
import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.common.services.employee.EmployeeService;
import com.emlogis.common.services.workflow.process.update.ResultPair;
import com.emlogis.common.services.workflow.process.update.asynch.common.WorkflowActionProxy;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.workflow.entities.WorkflowRequest;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;

import static com.emlogis.common.EmlogisUtils.fromJsonString;

/**
 * Created by user on 07.07.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AvailabilityActionProxy<T extends AvailabilityWorkflowRequest> extends WorkflowActionProxy {

    @EJB
    private EmployeeFacade employeeFacade;

    @EJB
    private EmployeeService employeeService;


    public ResultPair execute(WorkflowRequest request)
            throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        PrimaryKey employeePk = request.getInitiator().getPrimaryKey();
        try {
            switch (request.getAvailabilityRequestSubtype()) {
                case AvailcalUpdateParamsCDAvailDto: {
                    return availcalUpdateParamsCDAvailDto(request, employeePk);
                }
                case AvailcalUpdateParamsCDPrefDto: {
                    return availcalUpdateParamsCDPrefDto(request, employeePk);
                }
                case AvailcalUpdateParamsCIAvailDto: {
                    return availcalUpdateParamsCIAvailDto(request, employeePk);
                }
                case AvailcalUpdateParamsCIPrefDto: {
                    return availcalUpdateParamsCIPrefDto(request, employeePk);
                }
            }
            return new ResultPair(false, "Not supported type of availability change!");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            return new ResultPair(false, throwable.getMessage());
        }
    }

    private ResultPair availcalUpdateParamsCDAvailDto(
            WorkflowRequest request,
            PrimaryKey employeePk
    ) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        AvailcalUpdateParamsCDAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDAvailDto.class);
        try {
            employeeFacade.updateAvailcalCDAvail(employeePk, dto, null, null);
        } catch (ValidationException validationException) {
            return new ResultPair(false, validationException.getMessage());
        }
        return new ResultPair(true, "Created " + dto.getSelectedDates().size() * dto.getTimeFrames().size() + " CD time frames");
    }

    private ResultPair availcalUpdateParamsCDPrefDto(
            WorkflowRequest request,
            PrimaryKey employeePk
    ) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        AvailcalUpdateParamsCDPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCDPrefDto.class);
        AvailcalViewDto viewDto = employeeFacade.updateAvailcalCDPref(employeePk, dto, null, null);
        if (viewDto == null || viewDto.getAvailCDTimeFrames().isEmpty()) {
            return new ResultPair(false, "No availability record made");
        } else {
            return new ResultPair(true, "Created " + viewDto.getAvailCDTimeFrames().size() + " CD time frames");
        }
    }

    private ResultPair availcalUpdateParamsCIAvailDto(
            WorkflowRequest request,
            PrimaryKey employeePk
    ) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        AvailcalUpdateParamsCIAvailDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIAvailDto.class);
        AvailcalViewDto viewDto = employeeFacade.updateAvailcalCIAvail(employeePk, dto, null, null);
        if (viewDto == null || viewDto.getAvailCITimeFrames().isEmpty()) {
            return new ResultPair(false, "No availability record made");
        } else {
            return new ResultPair(true, "Created " + viewDto.getAvailCITimeFrames().size() + " CI time frames");
        }
    }

    private ResultPair availcalUpdateParamsCIPrefDto(
            WorkflowRequest request,
            PrimaryKey employeePk
    ) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        AvailcalUpdateParamsCIPrefDto dto = fromJsonString(request.getData(), AvailcalUpdateParamsCIPrefDto.class);
        AvailcalViewDto viewDto = employeeFacade.updateAvailcalCIPref(employeePk, dto, null, null);
        if (viewDto == null || viewDto.getAvailCITimeFrames().isEmpty()) {
            return new ResultPair(false, "No availability record made");
        } else {
            return new ResultPair(true, "Created " + viewDto.getAvailCITimeFrames().size() + " CI time frames");
        }
    }
}
