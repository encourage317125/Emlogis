package com.emlogis.common.services.workflow.process.intance;

import com.emlogis.model.employee.Employee;
import com.emlogis.model.tenant.UserAccount;
import com.emlogis.model.workflow.entities.WflProcess;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.workflow.enums.AvailabilityRequestSubtype;

import java.util.List;
import java.util.Map;

/**
 * Created by bbox on 22.10.15.
 */
public interface WorkflowRequestInitialDataSearchService {

    WorkflowRequest initialize(
            WflProcess abstractRequest,
            UserAccount userAccount,
            Employee employee,
            Long requestDate,
            String shiftId,
            AvailabilityRequestSubtype subtype,
            Long expiration,
            String data);

    WorkflowRequest initialize(
            WflProcess abstractRequest,
            UserAccount userAccount,
            Employee employee,
            String shiftId,
            Long expiration,
            String data,
            Integer recipientsSize,
            Map<String, List<String>> assignments);
}
