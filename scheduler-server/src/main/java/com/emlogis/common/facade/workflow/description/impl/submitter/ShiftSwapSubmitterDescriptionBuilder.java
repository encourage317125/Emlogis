package com.emlogis.common.facade.workflow.description.impl.submitter;

import com.emlogis.common.ResourcesBundle;
import com.emlogis.common.facade.workflow.description.DescriptionBuilder;
import com.emlogis.common.facade.workflow.description.annotations.RequestDescriptionBuilderQualifier;
import com.emlogis.common.facade.workflow.helper.RequestDatesHelper;
import com.emlogis.common.facade.workflow.helper.RequestLocaleHelper;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import com.emlogis.model.workflow.entities.WorkflowRequestPeer;
import org.joda.time.DateTimeZone;

import javax.ejb.*;
import javax.enterprise.inject.Default;
import java.util.ArrayList;
import java.util.Locale;

import static com.emlogis.workflow.WflUtil.recipientsApproved;
import static com.emlogis.workflow.enums.status.WorkflowRequestStatusDict.ADMIN_PENDING;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@Local
@Default
@RequestDescriptionBuilderQualifier(value = "SHIFT_SWAP_REQUEST_ORIGINATOR_DESCRIPTION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ShiftSwapSubmitterDescriptionBuilder implements DescriptionBuilder {

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
        StringBuilder builder = standardSwapDescriptionStart(request, locale, dtz);

        if (request.getRequestStatus().equals(ADMIN_PENDING)) {
            if (request.getRecipients().size() > 1) {
                builder.append(request.getRecipients().size());
                builder.append(" ");
                builder.append(resourcesBundle.getMessage(locale.getLanguage(), "description.wip.employees"));
                builder.append(", ");
                builder.append(recipientsApproved(request).size());
                builder.append(" ");
                builder.append(resourcesBundle.getMessage(locale.getLanguage(), "description.wip.accepted"));

            } else if (request.getRecipients().size() == 1) {
                WorkflowRequestPeer peer = new ArrayList<>(request.getRecipients()).get(0);
                builder.append(peer.getRecipient().reportName());
            }
            return builder.toString();
        }
        return standardSwapDescription(builder, request, locale);
    }

    private StringBuilder standardSwapDescriptionStart(WorkflowRequest workflowRequest, Locale locale, DateTimeZone dtz) {
        StringBuilder builder = new StringBuilder();
        builder.append(datesHelper.dateFormat(dtz, workflowRequest.getSubmitterShiftStartDateTime(), workflowRequest.getSubmitterShiftEndDateTime(), locale));
        builder.append(" ");
        builder.append(resourcesBundle.getMessage(locale.getLanguage(), "description.wip.to"));
        builder.append(" ");
        return builder;
    }

    private String standardSwapDescription(
            StringBuilder builder,
            WorkflowRequest workflowRequest,
            Locale locale
    ) {
        if (workflowRequest.getRecipients().size() > 1) {
            builder.append(workflowRequest.getRecipients().size());
            builder.append(" ");
            builder.append(resourcesBundle.getMessage(locale.getLanguage(), "description.wip.employees"));
        } else if (workflowRequest.getRecipients().size() == 1) {
            WorkflowRequestPeer peer = new ArrayList<>(workflowRequest.getRecipients()).get(0);
            builder.append(peer.getRecipient().reportName());
        }
        return builder.toString();
    }
}
