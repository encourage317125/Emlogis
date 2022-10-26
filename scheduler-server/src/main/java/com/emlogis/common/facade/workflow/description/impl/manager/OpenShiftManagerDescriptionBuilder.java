package com.emlogis.common.facade.workflow.description.impl.manager;

import com.emlogis.common.ResourcesBundle;
import com.emlogis.common.facade.workflow.description.DescriptionBuilder;
import com.emlogis.common.facade.workflow.description.annotations.RequestDescriptionBuilderQualifier;
import com.emlogis.common.facade.workflow.helper.RequestDatesHelper;
import com.emlogis.common.facade.workflow.helper.RequestLocaleHelper;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import org.joda.time.DateTimeZone;

import javax.ejb.*;
import javax.enterprise.inject.Default;
import java.util.Locale;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@Local
@Default
@RequestDescriptionBuilderQualifier(value = "OPEN_SHIFT_REQUEST_MANAGER_DESCRIPTION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class OpenShiftManagerDescriptionBuilder implements DescriptionBuilder {

    @EJB
    private WorkflowRequestService workflowRequestService;

    @EJB
    private ResourcesBundle resourcesBundle;

    @EJB
    private WorkflowRequestPeerService peerService;

    @EJB
    private RequestLocaleHelper localeHelper;

    @EJB
    private RequestDatesHelper datesHelper;

    @Override
    public String build(WorkflowRequest request, DateTimeZone dtz, Locale locale) {
        return datesHelper.dateFormat(dtz, request.getSubmitterShiftStartDateTime(),
                request.getSubmitterShiftEndDateTime(), locale);
    }
}
