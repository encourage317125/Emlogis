package com.emlogis.common.facade.workflow.process.submition;

import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.dto.process.request.submit.SubmitDto;
import com.emlogis.model.workflow.dto.process.response.SubmitRequestResultDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.exception.WorkflowServerException;

/**
 * Created by user on 21.08.15.
 */
public interface RequestSubmitFacade {

    /**
     * Method to submit and start new workflow process instance
     *
     * @param request       - {@link SubmitDto} DTO request data for new workflow process instance
     * @return
     * @throws WorkflowServerException
     */
    SubmitRequestResultDto submitRequest(
            UserAccount userAccount,
            Employee employee,
            SubmitDto request) throws WorkflowServerException;

    void removeSubmittedWorkflowProcess(WorkflowRequest workflowRequest, Employee requestedEmployee, String comment);

}
