package com.emlogis.common.facade.workflow.description.impl.manager;

import com.emlogis.common.facade.workflow.description.DescriptionBuilder;
import com.emlogis.common.facade.workflow.description.annotations.RequestDescriptionBuilderQualifier;
import com.emlogis.common.facade.workflow.helper.RequestDatesHelper;
import com.emlogis.common.facade.workflow.helper.RequestLocaleHelper;
import com.emlogis.common.services.employee.AbsenceTypeService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.workflow.dto.process.request.TimeOffRequestInfoDto;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import org.joda.time.DateTimeZone;

import javax.ejb.*;
import javax.enterprise.inject.Default;
import java.util.Locale;

import static com.emlogis.common.EmlogisUtils.fromJsonString;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@Local
@Default
@RequestDescriptionBuilderQualifier(value = "TIME_OFF_REQUEST_MANAGER_DESCRIPTION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class TimeOffManagerDescriptionBuilder implements DescriptionBuilder {

    @EJB
    private WorkflowRequestService workflowRequestService;

    @EJB
    private AbsenceTypeService absenceTypeService;

    @EJB
    private RequestLocaleHelper localeHelper;

    @EJB
    private RequestDatesHelper datesHelper;

    public String build(WorkflowRequest request, DateTimeZone dtz, Locale locale) {
        return absenceName(request) + " " + datesHelper.dateStr(dtz, request.getRequestDate(), locale);
    }

    private String absenceName(WorkflowRequest req) {
        TimeOffRequestInfoDto tori = fromJsonString(req.getData(), TimeOffRequestInfoDto.class);
        return absenceTypeService.getAbsenceType(new PrimaryKey(req.getTenantId(), tori.getAbsenceTypeId())).getName();
    }
}
