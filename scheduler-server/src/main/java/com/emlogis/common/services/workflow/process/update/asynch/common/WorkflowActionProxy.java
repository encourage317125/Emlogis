package com.emlogis.common.services.workflow.process.update.asynch.common;

import com.emlogis.common.facade.employee.EmployeeFacade;
import com.emlogis.common.services.workflow.TranslationParam;
import com.emlogis.common.services.workflow.WorkflowRequestTranslator;
import com.emlogis.model.PrimaryKey;
import com.emlogis.model.employee.AbsenceType;
import com.emlogis.model.employee.AvailabilityTimeFrame;
import com.emlogis.model.employee.Employee;
import com.emlogis.model.employee.dto.CDAvailabilityTimeFrameCreateDto;
import com.emlogis.model.employee.dto.CDAvailabilityTimeFrameDto;
import com.emlogis.rest.security.SessionService;

import javax.ejb.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import static com.emlogis.workflow.WflUtil.startOfDateInUTC;

/**
 * Created by user on 18.09.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public abstract class WorkflowActionProxy {

    @EJB
    private WorkflowRequestTranslator translator;

    @EJB
    private SessionService sessionService;


    @EJB
    private EmployeeFacade employeeFacade;

    protected String translate(
            String key,
            TranslationParam[] params
    ) {
        String token = sessionService.getTokenId();
        String language = sessionService.getSessionInfo(token).getLanguage();
        Locale locale = new Locale(language != null ? language : "en");
        return translator.getMessage(locale, key, params);
    }

    protected CDAvailabilityTimeFrameDto timeframe(
            Employee employee,
            AbsenceType absenceType,
            String reason,
            Long requestDate
    ) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        CDAvailabilityTimeFrameCreateDto dto = new CDAvailabilityTimeFrameCreateDto();
        dto.setReason(reason);
        dto.setEmployeeId(employee.getId());
        dto.setIsPTO(true);
        dto.setAbsenceTypeId(absenceType.getId());
        dto.setDurationInMinutes(24 * 60);
        dto.setStartDateTime(startOfDateInUTC(requestDate).getMillis());
        dto.setAvailabilityType(AvailabilityTimeFrame.AvailabilityType.UnAvail);
        return employeeFacade.createCDAvailabilityTimeFrame(
                new PrimaryKey(employee.getTenantId()), dto);
    }
}
