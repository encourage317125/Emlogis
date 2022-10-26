package com.emlogis.common.facade.workflow.description.impl.submitter;

import com.emlogis.common.ResourcesBundle;
import com.emlogis.common.facade.workflow.description.DescriptionBuilder;
import com.emlogis.common.facade.workflow.description.annotations.RequestDescriptionBuilderQualifier;
import com.emlogis.common.facade.workflow.helper.RequestDatesHelper;
import com.emlogis.common.facade.workflow.helper.RequestLocaleHelper;
import com.emlogis.common.services.workflow.peer.WorkflowRequestPeerService;
import com.emlogis.common.services.workflow.process.intance.WorkflowRequestService;
import com.emlogis.model.employee.dto.*;
import com.emlogis.model.workflow.entities.WorkflowRequest;
import org.joda.time.DateTimeZone;

import javax.ejb.*;
import javax.enterprise.inject.Default;
import java.util.Collection;
import java.util.Locale;

import static com.emlogis.common.EmlogisUtils.fromJsonString;
import static com.emlogis.model.employee.dto.AvailcalUpdateParamsAvailDto.AvailAction.AVAILABLE_FOR_DAY;
import static com.emlogis.model.employee.dto.AvailcalUpdateParamsAvailDto.AvailAction.AVAILABLE_FOR_TIMEFRAMES;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@Local
@Default
@RequestDescriptionBuilderQualifier(value = "AVAILABILITY_REQUEST_ORIGINATOR_DESCRIPTION")
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class AvailabilitySubmitterDescriptionBuilder implements DescriptionBuilder {

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
        StringBuilder builder = new StringBuilder();
        Collection<Long> selectedDates = null;

        switch (request.getAvailabilityRequestSubtype()) {
            case AvailcalUpdateParamsCDAvailDto: {
                AvailcalUpdateParamsCDAvailDto availBean = fromJsonString(request.getData(),
                        AvailcalUpdateParamsCDAvailDto.class);
                selectedDates = availBean.getSelectedDates();
                Boolean isAvailability = availBean.getAction().equals(AVAILABLE_FOR_DAY) ||
                        availBean.getAction().equals(AVAILABLE_FOR_TIMEFRAMES);
                String message = isAvailability ? resourcesBundle.getMessage(locale.getLanguage(), "description.available") :
                        resourcesBundle.getMessage(locale.getLanguage(), "description.unavailable");
                builder.append(message + " ");
                builder.append(resourcesBundle.getMessage(locale.getLanguage(), "description.on") + " ");
                int lastIndex = selectedDates.size() - 1;
                int i = 0;
                for (Long selectedDate : selectedDates) {
                    i++;
                    builder.append(datesHelper.dateStr(dtz, selectedDate, locale) + ((i == lastIndex) ? ", " : ""));
                }
                return builder.toString();
            }
            case AvailcalUpdateParamsCIAvailDto: {
                AvailcalUpdateParamsCIAvailDto availBean = fromJsonString(request.getData(),
                        AvailcalUpdateParamsCIAvailDto.class);
                AvailcalUpdateParamsCIDaySelections daySelections = availBean.getSelectedDays();
                Boolean isAvailability = availBean.getAction().equals(AVAILABLE_FOR_DAY) ||
                        availBean.getAction().equals(AVAILABLE_FOR_TIMEFRAMES);
                String message = isAvailability ? resourcesBundle.getMessage(locale.getLanguage(), "description.available") :
                        resourcesBundle.getMessage(locale.getLanguage(), "description.unavailable");

                builder.append(resourcesBundle.getMessage(locale.getLanguage(), "description.recurring") + " ");
                builder.append(message+" ");
                builder.append(resourcesBundle.getMessage(locale.getLanguage(), "description.for") + " ");
                int lastIndex = daySelections.dayKeysSelected().size() - 1;
                int i = 0;
                for (String dayTranslate : daySelections.dayKeysSelected()) {
                    i++;
                    builder.append(resourcesBundle.getMessage(locale.getLanguage(), dayTranslate) + ((i == lastIndex) ? ", " : ""));
                }
                return builder.toString();
            }
        }

        switch (request.getAvailabilityRequestSubtype()) {
            case AvailcalUpdateParamsCDPrefDto: {
                AvailcalUpdateParamsCDPrefDto availBean = fromJsonString(request.getData(), AvailcalUpdateParamsCDPrefDto.class);
                selectedDates = availBean.getSelectedDates();
                String message = resourcesBundle.getMessage(locale.getLanguage(), "description.preferences");
                builder.append(message + " ");
                builder.append(resourcesBundle.getMessage(locale.getLanguage(), "description.on") + " ");
                int lastIndex = selectedDates.size() - 1;
                int i = 0;
                for (Long selectedDate : selectedDates) {
                    i++;
                    builder.append(datesHelper.dateStr(dtz, selectedDate, locale) + ((i == lastIndex) ? ", " : ""));
                }
                return builder.toString();
            }
            case AvailcalUpdateParamsCIPrefDto: {
                AvailcalUpdateParamsCIPrefDto availBean = fromJsonString(request.getData(),
                        AvailcalUpdateParamsCIPrefDto.class);
                AvailcalUpdateParamsCIDaySelections daySelections = availBean.getSelectedDays();
                String message = resourcesBundle.getMessage(locale.getLanguage(), "description.preferences");

                builder.append(resourcesBundle.getMessage(locale.getLanguage(), "description.recurring") + " ");
                builder.append(message+" ");
                builder.append(resourcesBundle.getMessage(locale.getLanguage(), "description.for") + " ");
                int lastIndex = selectedDates.size() - 1;
                int i = 0;
                for (String dayTranslate : daySelections.dayKeysSelected()) {
                    i++;
                    builder.append(resourcesBundle.getMessage(locale.getLanguage(), dayTranslate) + ((i == lastIndex) ? ", " : ""));
                }
                return builder.toString();
            }
        }
        return null;
    }
}
