package com.emlogis.common.facade.workflow.helper;

import com.emlogis.model.employee.Employee;
import com.emlogis.model.structurelevel.Site;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import javax.ejb.*;
import java.text.DateFormat;
import java.util.Locale;

/**
 * Created by user on 20.08.15.
 */
@Stateless
@LocalBean
@TransactionManagement(value = TransactionManagementType.CONTAINER)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class RequestDatesHelper {

    @EJB
    private ServiceHelper serviceHelper;

    public DateTimeZone identifyEmployeeDateTimeZone(Employee employee) {
        Site employeeSite = serviceHelper.site(employee.getHomeTeam());
        if (employeeSite.getTimeZone() == null) {
            return DateTimeZone.UTC;
        }
        return employeeSite.getTimeZone();
    }


    public String dateFormat(DateTimeZone dtz, Long timefromUTC, Long timeToUTC, Locale locale) {
        return dateStr(dtz, timefromUTC, locale) + " " + timeStr(dtz, timefromUTC, locale) + " - " + timeStr(dtz, timeToUTC, locale);
    }


    public String timeStr(DateTimeZone dtz, Long timeUTC, Locale locale) {
        DateTimeFormatter formatter = DateTimeFormat.forStyle("-S").withLocale(locale);
        String output = formatter.print(dtz.convertUTCToLocal(timeUTC));
        return output;
    }

    public String dateStr(DateTimeZone dtz, Long timeUTC, Locale locale) {
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        String formattedDate = df.format(dtz.convertUTCToLocal(timeUTC));
        return formattedDate;
    }
}
